package com.turno.turni;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class TurnoService {

    private final TurnoRepository repository;

    public TurnoService(TurnoRepository repository) {
        this.repository = repository;
    }

    public List<TurnoResponse> getRange(LocalDate dal, LocalDate al) {
        return repository.findByDataBetweenOrderByDataAscInizioAsc(dal, al)
                .stream().map(TurnoResponse::from).toList();
    }

    public List<TurnoResponse> getRangePerUtente(Long userId, LocalDate dal, LocalDate al) {
        return repository.findByUserIdAndDataBetweenOrderByDataAscInizioAsc(userId, dal, al)
                .stream().map(TurnoResponse::from).toList();
    }

    @Transactional
    public TurnoResponse crea(TurnoRequest req) {
        if (!req.fine().isAfter(req.inizio()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'orario di fine deve essere dopo l'inizio");
        if (repository.existsOverlap(req.userId(), req.data(), req.inizio(), req.fine()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Turno sovrapposto a uno esistente");
        return TurnoResponse.from(repository.save(new Turno(req.userId(), req.data(), req.inizio(), req.fine())));
    }

    // crea i turni validi e salta quelli sovrapposti o con orari sbagliati (usato
    // da "copia settimana precedente")
    @Transactional
    public BatchResult creaBatch(List<TurnoRequest> requests) {
        List<TurnoResponse> creati = new ArrayList<>();
        int saltati = 0;
        for (TurnoRequest req : requests) {
            if (!req.fine().isAfter(req.inizio())
                    || repository.existsOverlap(req.userId(), req.data(), req.inizio(), req.fine())) {
                saltati++;
                continue;
            }
            // saveAndFlush così il controllo sovrapposizioni vede anche i turni appena
            // creati nello stesso batch
            creati.add(TurnoResponse.from(repository.saveAndFlush(
                    new Turno(req.userId(), req.data(), req.inizio(), req.fine()))));
        }
        return new BatchResult(creati, saltati);
    }

    @Transactional
    public void elimina(Long id) {
        if (!repository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }
}
