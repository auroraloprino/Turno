package com.turno.timbratura;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.turno.timbratura.kafka.TimbraturaAuditEvent;
import com.turno.timbratura.kafka.TimbraturaEventProducer;

@Service
@Transactional(readOnly = true)
public class TimbraturaService {

    private final TimbraturaRepository repository;
    private final TimbraturaEventProducer producer;

    public TimbraturaService(TimbraturaRepository repository, TimbraturaEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    public TimbraturaResponse timbra(Long userId) {
        var ultima = repository.findTopByUserIdOrderByTimestampDesc(userId);
        TipoTimbratura prossimo = ultima
                .map(t -> t.getTipo() == TipoTimbratura.ENTRATA ? TipoTimbratura.USCITA : TipoTimbratura.ENTRATA)
                .orElse(TipoTimbratura.ENTRATA);
        if (ultima.isPresent() && ultima.get().getTipo() == prossimo)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Timbratura già registrata");
        Instant now = Instant.now();
        Timbratura t = repository.save(new Timbratura(userId, prossimo, now));
        producer.publish(new TimbraturaAuditEvent(userId, prossimo.name(), now));
        return TimbraturaResponse.from(t);
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
