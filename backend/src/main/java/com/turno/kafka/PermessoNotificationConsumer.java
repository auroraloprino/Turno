package com.turno.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PermessoNotificationConsumer {

    private final SimpMessagingTemplate ws;

    public PermessoNotificationConsumer(SimpMessagingTemplate ws) {
        this.ws = ws;
    }

    @KafkaListener(topics = "permesso-stato", groupId = "turno-group")
    public void consume(PermessoStatoEvent event) {
        ws.convertAndSend(
            "/topic/notifiche/" + event.userId(),
            new Notifica("permesso", "La tua richiesta è stata " + event.stato().name().toLowerCase())
        );
    }

    public record Notifica(String tipo, String messaggio) {}
}
