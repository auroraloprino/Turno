package com.turno.turni;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.turno.common.mongo.IdNumerici;

@Service
public class TurnoService {

    private final TurnoRepository repository;
    private final MongoOperations mongo;
    private final IdNumerici idNumerici;

    public TurnoService(TurnoRepository repository, MongoOperations mongo, IdNumerici idNumerici) {
        this.repository = repository;
        this.mongo = mongo;
        this.idNumerici = idNumerici;
    }

    public List<TurnoResponse> getRange(LocalDate dal, LocalDate al) {
        return repository.findRange(dal, al)
                .stream().map(TurnoResponse::from).toList();
    }

    public List<TurnoResponse> getRangePerUtente(Long userId, LocalDate dal, LocalDate al) {
        return repository.findRangePerUtente(userId, dal, al)
                .stream().map(TurnoResponse::from).toList();
    }

    public TurnoResponse crea(TurnoRequest req) {
        if (!req.fine().isAfter(req.inizio()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'orario di fine deve essere dopo l'inizio");
        if (repository.existsOverlap(req.userId(), req.data(), req.inizio(), req.fine()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Turno sovrapposto a uno esistente");
        return TurnoResponse.from(repository.save(new Turno(req.userId(), req.data(), req.inizio(), req.fine())));
    }

    // crea i turni validi e salta quelli sovrapposti o con orari sbagliati (usato da "copia settimana precedente")
    public BatchResult creaBatch(List<TurnoRequest> requests) {
        List<TurnoRequest> validi = requests.stream().filter(r -> r.fine().isAfter(r.inizio())).toList();
        int saltati = requests.size() - validi.size();
        if (validi.isEmpty())
            return new BatchResult(List.of(), saltati);

        // una sola query per i turni già presenti, poi il controllo sovrapposizioni avviene in memoria
        // (un giro su Atlas per ogni turno con centinaia di turni superava il timeout del gateway)
        Set<Long> utenti = validi.stream().map(TurnoRequest::userId).collect(Collectors.toSet());
        LocalDate dal = validi.stream().map(TurnoRequest::data).min(LocalDate::compareTo).orElseThrow();
        LocalDate al = validi.stream().map(TurnoRequest::data).max(LocalDate::compareTo).orElseThrow();
        Map<String, List<Turno>> occupati = new HashMap<>();
        for (Turno t : repository.findPerUtentiNelPeriodo(utenti, dal, al))
            occupati.computeIfAbsent(chiave(t.getUserId(), t.getData()), k -> new ArrayList<>()).add(t);

        List<Turno> nuovi = new ArrayList<>();
        for (TurnoRequest req : validi) {
            List<Turno> giorno = occupati.computeIfAbsent(chiave(req.userId(), req.data()), k -> new ArrayList<>());
            boolean sovrapposto = giorno.stream()
                    .anyMatch(t -> req.inizio().isBefore(t.getFine()) && req.fine().isAfter(t.getInizio()));
            if (sovrapposto) {
                saltati++;
                continue;
            }
            Turno nuovo = new Turno(req.userId(), req.data(), req.inizio(), req.fine());
            //  aggiunto subito, così vale anche contro gli altri turni dello stesso batch
            giorno.add(nuovo);
            nuovi.add(nuovo);
        }

        if (!nuovi.isEmpty()) {
            long primoId = idNumerici.riserva("turni", nuovi.size());
            for (int i = 0; i < nuovi.size(); i++)
                nuovi.get(i).setId(primoId + i);
            mongo.insertAll(nuovi);
        }
        return new BatchResult(nuovi.stream().map(TurnoResponse::from).toList(), saltati);
    }

    private static String chiave(Long userId, LocalDate data) {
        return userId + "|" + data;
    }

    public void eliminaPerUtente(Long userId) {
        repository.deleteByUserId(userId);
    }

    public void elimina(Long id) {
        if (!repository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }
}
