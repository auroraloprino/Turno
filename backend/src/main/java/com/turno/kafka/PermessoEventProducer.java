package com.turno.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PermessoEventProducer {

    private final KafkaTemplate<String, PermessoStatoEvent> kafka;

    public PermessoEventProducer(KafkaTemplate<String, PermessoStatoEvent> kafka) {
        this.kafka = kafka;
    }

    public void publish(PermessoStatoEvent event) {
        kafka.send("permesso-stato", String.valueOf(event.userId()), event);
    }
}
