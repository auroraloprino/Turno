package com.turno.chat.kafka;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PermessoNotificationConsumer {

    private final SimpMessagingTemplate ws;

    public PermessoNotificationConsumer(SimpMessagingTemplate ws) {
        this.ws = ws;
    }

    @KafkaListener(topics = "permesso-stato", groupId = "chat-group")
    public void consume(PermessoStatoEvent event) {
        ws.convertAndSend(
                "/topic/notifiche/" + event.userId(),
                Map.of("tipo", "permesso", "messaggio", "La tua richiesta è stata " + event.stato().toLowerCase()));
    }
}
