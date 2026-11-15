package com.turno.chat;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.common.mongo.ConIdNumerico;

@Document(collection = "messaggi")
@CompoundIndex(def = "{ 'conversazioneId': 1, 'timestamp': 1 }")
public class Messaggio implements ConIdNumerico {

    @Id
    private Long id;

    // tengo solo l'id della conversazione
    private Long conversazioneId;

    private Long senderId;

    private String senderName;

    private String testo;

    // id del file in GridFS, null per i messaggi di solo testo
    private String immagineId;

    private Instant timestamp;

    private boolean modificato = false;

    private boolean eliminato = false;

    public Messaggio() {
    }

    public Messaggio(Long conversazioneId, Long senderId, String senderName, String testo, Instant timestamp) {
        this.conversazioneId = conversazioneId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.testo = testo;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getImmagineId() {
        return immagineId;
    }

    public void setImmagineId(String immagineId) {
        this.immagineId = immagineId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversazioneId() {
        return conversazioneId;
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
}
