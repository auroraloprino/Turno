package com.turno.timbratura;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TimbraturaService {

    private final TimbraturaRepository repository;

    public TimbraturaService(TimbraturaRepository repository) {
        this.repository = repository;
    }

    public TimbraturaResponse timbra(Long userId) {
        var ultima = repository.findTopByUserIdOrderByTimestampDesc(userId);
        TipoTimbratura prossimo = ultima
                .map(t -> t.getTipo() == TipoTimbratura.ENTRATA ? TipoTimbratura.USCITA : TipoTimbratura.ENTRATA)
                .orElse(TipoTimbratura.ENTRATA);
        if (ultima.isPresent() && ultima.get().getTipo() == prossimo)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Timbratura già registrata");
        return TimbraturaResponse.from(repository.save(new Timbratura(userId, prossimo, Instant.now())));
    }

    public void eliminaPerUtente(Long userId) {
        repository.deleteByUserId(userId);
    }

    public List<TimbraturaResponse> storico(Long userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId)
                .stream().map(TimbraturaResponse::from).toList();
    }

    public List<TimbraturaResponse> tutte() {
        return repository.findAllByOrderByTimestampDesc()
                .stream().map(TimbraturaResponse::from).toList();
    }
}
