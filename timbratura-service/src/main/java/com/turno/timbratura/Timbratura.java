package com.turno.timbratura;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timbrature")
public class Timbratura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTimbratura tipo;

    @Column(nullable = false)
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
