package com.turno.timbratura;

import java.time.Instant;

public record TimbraturaResponse(Long id, Long userId, String tipo, Instant timestamp) {
    public static TimbraturaResponse from(Timbratura t) {
        return new TimbraturaResponse(t.getId(), t.getUserId(), t.getTipo().name(), t.getTimestamp());
    }
}
