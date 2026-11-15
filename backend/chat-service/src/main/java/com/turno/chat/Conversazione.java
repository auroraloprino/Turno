package com.turno.chat;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.common.mongo.ConIdNumerico;

@Document(collection = "conversazioni")
public class Conversazione implements ConIdNumerico {

    @Id
    private Long id;

    private String tipo;

    private String nome;

    // array dentro il documento
    @Indexed
    private Set<Long> partecipantiIds = new HashSet<>();

    public Conversazione() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public Set<Long> getPartecipantiIds() {
        return partecipantiIds;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPartecipantiIds(Set<Long> ids) {
        this.partecipantiIds = ids;
    }
}
