package com.turno.chat;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "messaggi")
public class Messaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversazione_id", nullable = false)
    private Conversazione conversazione;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String testo;

    @Column(name = "allegato_url")
    private String allegatoUrl;

    @Column(name = "allegato_nome")
    private String allegatoNome;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private boolean modificato = false;

    @Column(nullable = false)
    private boolean eliminato = false;

    public Messaggio() {
    }

    public Messaggio(Conversazione conversazione, Long senderId, String senderName, String testo, Instant timestamp) {
        this.conversazione = conversazione;
        this.senderId = senderId;
        this.senderName = senderName;
        this.testo = testo;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Conversazione getConversazione() {
        return conversazione;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTesto() {
        return testo;
    }

    public String getAllegatoUrl() {
        return allegatoUrl;
    }

    public String getAllegatoNome() {
        return allegatoNome;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isModificato() {
        return modificato;
    }

    public boolean isEliminato() {
        return eliminato;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public void setModificato(boolean v) {
        this.modificato = v;
    }

    public void setEliminato(boolean v) {
        this.eliminato = v;
    }

    public void setAllegatoUrl(String url) {
        this.allegatoUrl = url;
    }

    public void setAllegatoNome(String nome) {
        this.allegatoNome = nome;
    }
}
