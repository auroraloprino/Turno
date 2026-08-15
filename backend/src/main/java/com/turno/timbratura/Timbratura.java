package com.turno.timbratura;

import com.turno.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "timbrature")
public class Timbratura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTimbratura tipo;

    @Column(nullable = false)
    private Instant timestamp;

    public Timbratura() {}

    public Timbratura(User user, TipoTimbratura tipo, Instant timestamp) {
        this.user = user;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public TipoTimbratura getTipo() { return tipo; }
    public Instant getTimestamp() { return timestamp; }
}
