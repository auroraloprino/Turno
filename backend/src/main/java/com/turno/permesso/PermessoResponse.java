package com.turno.permesso;

import java.time.LocalDate;
import java.time.LocalTime;

public record PermessoResponse(
        Long id,
        Long userId,
        String userName,
        String tipo,
        LocalDate dal,
        LocalDate al,
        LocalTime oraInizio,
        LocalTime oraFine,
        String note,
        String stato
) {
    public static PermessoResponse from(Permesso p) {
        return new PermessoResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getName(),
                p.getTipo(),
                p.getDal(),
                p.getAl(),
                p.getOraInizio(),
                p.getOraFine(),
                p.getNote(),
                p.getStato().name()
        );
    }
}
