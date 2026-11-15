package com.turno.common.outbox;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.common.mongo.ConIdNumerico;

@Document(collection = "outbox_events")
@TypeAlias("EventoOutbox")
public class EventoOutbox implements ConIdNumerico {

    public static final String IN_ATTESA = "IN_ATTESA";
    public static final String PUBBLICATO = "PUBBLICATO";

    @Id
    private Long id;

    private String topic;

    private String chiave;

    private Map<String, Object> payload;

    private Instant creatoIl;

    @Indexed
    private String stato = IN_ATTESA;

    private int tentativi = 0;

    public EventoOutbox() {
    }

    public EventoOutbox(String topic, String chiave, Map<String, Object> payload) {
        this.topic = topic;
        this.chiave = chiave;
        this.payload = payload;
        this.creatoIl = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public String getChiave() {
        return chiave;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getStato() {
        return stato;
    }

    public int getTentativi() {
        return tentativi;
    }

    public void pubblicato() {
        this.stato = PUBBLICATO;
    }

    public void tentativoFallito() {
        this.tentativi++;
    }
}
