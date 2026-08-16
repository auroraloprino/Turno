package com.turno.chat;

import com.turno.user.User;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
        name = "conversazione_partecipanti",
        joinColumns = @JoinColumn(name = "conversazione_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> partecipanti = new HashSet<>();

    public Conversazione() {}

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getNome() { return nome; }
    public Set<User> getPartecipanti() { return partecipanti; }

    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setNome(String nome) { this.nome = nome; }
    public void setPartecipanti(Set<User> partecipanti) { this.partecipanti = partecipanti; }
}
