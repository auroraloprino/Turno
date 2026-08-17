package com.turno.kafka;

import jakarta.persistence.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TimbraturaAuditConsumer {

    @PersistenceContext
    private EntityManager em;

    @KafkaListener(topics = "timbratura-audit", groupId = "turno-group")
    @jakarta.transaction.Transactional
    public void consume(TimbraturaAuditEvent event) {
        em.createNativeQuery(
            "INSERT INTO audit_log (user_id, tipo, timestamp) VALUES (:userId, :tipo, :ts)"
        )
        .setParameter("userId", event.userId())
        .setParameter("tipo", event.tipo().name())
        .setParameter("ts", event.timestamp())
        .executeUpdate();
    }
}
