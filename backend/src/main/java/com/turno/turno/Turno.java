package com.turno.turno;

import com.turno.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turni")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime inizio;

    @Column(nullable = false)
    private LocalTime fine;

    public Turno() {}

    public Turno(User user, LocalDate data, LocalTime inizio, LocalTime fine) {
        this.user = user;
        this.data = data;
        this.inizio = inizio;
        this.fine = fine;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public LocalDate getData() { return data; }
    public LocalTime getInizio() { return inizio; }
    public LocalTime getFine() { return fine; }
}
