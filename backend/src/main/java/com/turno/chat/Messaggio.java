package com.turno.chat;

import com.turno.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "messaggi")
public class Messaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversazione_id", nullable = false)
    private Conversazione conversazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

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

    public Messaggio() {}

    public Messaggio(Conversazione conversazione, User sender, String testo, Instant timestamp) {
        this.conversazione = conversazione;
        this.sender = sender;
        this.testo = testo;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public Conversazione getConversazione() { return conversazione; }
    public User getSender() { return sender; }
    public String getTesto() { return testo; }
    public String getAllegatoUrl() { return allegatoUrl; }
    public String getAllegatoNome() { return allegatoNome; }
    public Instant getTimestamp() { return timestamp; }
    public boolean isModificato() { return modificato; }
    public boolean isEliminato() { return eliminato; }
    public void setTesto(String testo) { this.testo = testo; }
    public void setModificato(boolean modificato) { this.modificato = modificato; }
    public void setEliminato(boolean eliminato) { this.eliminato = eliminato; }
    public void setAllegatoUrl(String allegatoUrl) { this.allegatoUrl = allegatoUrl; }
    public void setAllegatoNome(String allegatoNome) { this.allegatoNome = allegatoNome; }
}
