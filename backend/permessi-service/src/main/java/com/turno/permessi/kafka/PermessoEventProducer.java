package com.turno.permessi.kafka;

import org.springframework.stereotype.Component;

import com.turno.permessi.outbox.Outbox;

@Component
public class PermessoEventProducer {

    private final Outbox outbox;

    public PermessoEventProducer(Outbox outbox) {
        this.outbox = outbox;
    }

    // scrive nell'outbox e ci pensa OutboxPublisher a pubblicare
    public void publish(PermessoStatoEvent event) {
        outbox.registra("permesso-stato", event.userId(), event);
    }
}
