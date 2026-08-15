package com.turno.timbratura;

import com.turno.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class TimbraturaService {

    private final TimbraturaRepository repository;

    public TimbraturaService(TimbraturaRepository repository) {
        this.repository = repository;
    }

    public TimbraturaResponse timbra(User user) {
        var ultima = repository.findTopByUserIdOrderByTimestampDesc(user.getId());

        TipoTimbratura prossimo = ultima
                .map(t -> t.getTipo() == TipoTimbratura.ENTRATA ? TipoTimbratura.USCITA : TipoTimbratura.ENTRATA)
                .orElse(TipoTimbratura.ENTRATA);

        if (ultima.isPresent() && ultima.get().getTipo() == prossimo)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Timbratura già registrata");

        Timbratura t = repository.save(new Timbratura(user, prossimo, Instant.now()));
        return TimbraturaResponse.from(t);
    }

    public List<TimbraturaResponse> storico(Long userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId)
                .stream().map(TimbraturaResponse::from).toList();
    }

    public List<TimbraturaResponse> tutте() {
        return repository.findAllByOrderByTimestampDesc()
                .stream().map(TimbraturaResponse::from).toList();
    }
}
