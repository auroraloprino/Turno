package com.turno.permessi.outbox;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final EventoOutboxRepository repository;
    private final KafkaTemplate<String, Object> kafka;

    public OutboxPublisher(EventoOutboxRepository repository, KafkaTemplate<String, Object> kafka) {
        this.repository = repository;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelay = 1000)
    public void pubblicaInAttesa() {
        for (EventoOutbox evento : repository.findTop100ByStatoOrderByIdAsc(EventoOutbox.IN_ATTESA)) {
            try {
                kafka.send(evento.getTopic(), evento.getChiave(), evento.getPayload()).get(15, TimeUnit.SECONDS);
                evento.pubblicato();
                repository.save(evento);
            } catch (Exception e) {
                evento.tentativoFallito();
                repository.save(evento);
                log.warn("Evento outbox {} non pubblicato (tentativo {}), riprovo al prossimo giro", evento.getId(), evento.getTentativi());
                return;
            }
        }
    }
}
