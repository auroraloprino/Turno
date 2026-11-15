package com.turno.permessi;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.permessi.mongo.ConIdNumerico;

@Document(collection = "permessi")
public class Permesso implements ConIdNumerico {

    @Id
    private Long id;

    @Indexed
    private Long userId;

    private String tipo;

    private LocalDate dal;

    private LocalDate al;

    private LocalTime oraInizio;
    private LocalTime oraFine;

    private String note;

    private String certificatoUrl;

    @Indexed
    private StatoPermesso stato = StatoPermesso.IN_ATTESA;

    public Permesso() {
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

    public String getTipo() {
        return tipo;
    }

    public LocalDate getDal() {
        return dal;
    }

    public LocalDate getAl() {
        return al;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public String getNote() {
        return note;
    }

    public String getCertificatoUrl() {
        return certificatoUrl;
    }

    public StatoPermesso getStato() {
        return stato;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDal(LocalDate dal) {
        this.dal = dal;
    }

    public void setAl(LocalDate al) {
        this.al = al;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCertificatoUrl(String url) {
        this.certificatoUrl = url;
    }

    public void setStato(StatoPermesso stato) {
        this.stato = stato;
    }
}
