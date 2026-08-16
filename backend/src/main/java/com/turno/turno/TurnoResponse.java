package com.turno.turno;

import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoResponse(
        Long id,
        Long userId,
        String userName,
        LocalDate data,
        LocalTime inizio,
        LocalTime fine
) {
    public static TurnoResponse from(Turno t) {
        return new TurnoResponse(
                t.getId(),
                t.getUser().getId(),
                t.getUser().getName(),
                t.getData(),
                t.getInizio(),
                t.getFine()
        );
    }
}
