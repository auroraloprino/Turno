package com.turno.permesso;

import java.time.LocalDate;
import java.time.LocalTime;

import com.turno.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "permessi")
public class Permesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private LocalDate dal;

    @Column(nullable = false)
    private LocalDate al;

    private LocalTime oraInizio;
    private LocalTime oraFine;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String certificatoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoPermesso stato = StatoPermesso.IN_ATTESA;

    public Permesso() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
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

    public void setCertificatoUrl(String certificatoUrl) {
        this.certificatoUrl = certificatoUrl;
    }

    public void setStato(StatoPermesso stato) {
        this.stato = stato;
    }
}
