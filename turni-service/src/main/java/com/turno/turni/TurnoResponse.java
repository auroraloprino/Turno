package com.turno.turni;

import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoResponse(Long id, Long userId, LocalDate data, LocalTime inizio, LocalTime fine) {
    public static TurnoResponse from(Turno t) {
        return new TurnoResponse(t.getId(), t.getUserId(), t.getData(), t.getInizio(), t.getFine());
    }
}
