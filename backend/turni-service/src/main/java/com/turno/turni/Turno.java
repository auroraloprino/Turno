package com.turno.turni;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.common.mongo.ConIdNumerico;

@Document(collection = "turni")
@CompoundIndex(def = "{ 'data': 1, 'inizio': 1 }")
// serve al controllo sovrapposizioni che cerca sempre per utente e giorno
@CompoundIndex(def = "{ 'userId': 1, 'data': 1 }")
public class Turno implements ConIdNumerico {

    @Id
    private Long id;

    private Long userId;

    private LocalDate data;

    private LocalTime inizio;

    private LocalTime fine;

    public Turno() {
    }

    public Turno(Long userId, LocalDate data, LocalTime inizio, LocalTime fine) {
        this.userId = userId;
        this.data = data;
        this.inizio = inizio;
        this.fine = fine;
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

    public LocalDate getData() {
        return data;
    }

    public LocalTime getInizio() {
        return inizio;
    }

    public LocalTime getFine() {
        return fine;
    }
}
