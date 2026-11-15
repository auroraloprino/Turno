package com.turno.permessi.outbox;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Outbox {

    private final EventoOutboxRepository repository;
    private final ObjectMapper json;

    public Outbox(EventoOutboxRepository repository, ObjectMapper json) {
        this.repository = repository;
        this.json = json;
    }

    // va chiamato dentro la stessa transazione che salva i dati, così evento e dati nascono insieme (o non)
    public void registra(String topic, Object chiave, Object evento) {
        Map<String, Object> payload = json.convertValue(evento, json.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        repository.save(new EventoOutbox(topic, String.valueOf(chiave), payload));
    }
}
