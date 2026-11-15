package com.turno.common.outbox;

import java.util.Map;

import org.springframework.data.mongodb.core.MongoOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Outbox {

    private final MongoOperations mongo;
    private final ObjectMapper json;

    public Outbox(MongoOperations mongo, ObjectMapper json) {
        this.mongo = mongo;
        this.json = json;
    }

    // va chiamato dentro la stessa transazione che salva i dati, così evento e dati nascono insieme (o non)
    public void registra(String topic, Object chiave, Object evento) {
        Map<String, Object> payload = json.convertValue(evento,
                json.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        mongo.insert(new EventoOutbox(topic, String.valueOf(chiave), payload));
    }
}
