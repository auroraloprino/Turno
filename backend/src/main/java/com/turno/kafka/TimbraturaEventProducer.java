package com.turno.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TimbraturaEventProducer {

    private final KafkaTemplate<String, TimbraturaAuditEvent> kafka;

    public TimbraturaEventProducer(KafkaTemplate<String, TimbraturaAuditEvent> kafka) {
        this.kafka = kafka;
    }

    public void publish(TimbraturaAuditEvent event) {
        kafka.send("timbratura-audit", String.valueOf(event.userId()), event);
    }
}
