package com.turno.turni;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "turni")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime inizio;

    @Column(nullable = false)
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
