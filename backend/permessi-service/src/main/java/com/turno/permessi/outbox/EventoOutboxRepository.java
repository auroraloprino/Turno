package com.turno.permessi.outbox;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventoOutboxRepository extends MongoRepository<EventoOutbox, Long> {
    List<EventoOutbox> findTop100ByStatoOrderByIdAsc(String stato);
}
