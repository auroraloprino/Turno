package com.turno.timbratura;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.timbratura.mongo.ConIdNumerico;

@Document(collection = "timbrature")
// copre sia lo storico di un utente sia la ricerca dell'ultima timbratura
@CompoundIndex(def = "{ 'userId': 1, 'timestamp': -1 }")
public class Timbratura implements ConIdNumerico {

    @Id
    private Long id;

    private Long userId;

    private TipoTimbratura tipo;

    private Instant timestamp;

    public Timbratura() {
    }

    public Timbratura(Long userId, TipoTimbratura tipo, Instant timestamp) {
        this.userId = userId;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public TipoTimbratura getTipo() {
        return tipo;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
