package com.turno.timbratura;

import java.time.Instant;

public record TimbraturaResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        String tipo,
        Instant timestamp
) {
    public static TimbraturaResponse from(Timbratura t) {
        return new TimbraturaResponse(
                t.getId(),
                t.getUser().getId(),
                t.getUser().getName(),
                t.getUser().getEmail(),
                t.getTipo().name(),
                t.getTimestamp()
        );
    }
}
