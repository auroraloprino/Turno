package com.turno.chat;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversazioni")
public class Conversazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, length = 100)
    private String nome;

    @ElementCollection
    @CollectionTable(name = "conversazione_partecipanti", joinColumns = @JoinColumn(name = "conversazione_id"))
    @Column(name = "user_id")
    private Set<Long> partecipantiIds = new HashSet<>();

    public Conversazione() {
    }

    public Long getId() {
        return id;
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
