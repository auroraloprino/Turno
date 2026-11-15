package com.turno.turni;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TurnoService {

    private final TurnoRepository repository;

    public TurnoService(TurnoRepository repository) {
        this.repository = repository;
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
        List<TurnoResponse> creati = new ArrayList<>();
        int saltati = 0;
        for (TurnoRequest req : requests) {
            if (!req.fine().isAfter(req.inizio())
                    || repository.existsOverlap(req.userId(), req.data(), req.inizio(), req.fine())) {
                saltati++;
                continue;
            }
            // ogni save è subito su Mongo, quindi il controllo vede anche i turni appena creati nel batch
            creati.add(TurnoResponse.from(repository.save(
                    new Turno(req.userId(), req.data(), req.inizio(), req.fine()))));
        }
        return new BatchResult(creati, saltati);
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
