package com.turno.permessi;

import java.time.LocalDate;
import java.time.LocalTime;

public record PermessoResponse(
        Long id, Long userId, String tipo,
        LocalDate dal, LocalDate al,
        LocalTime oraInizio, LocalTime oraFine,
        String note, String certificatoUrl, String stato) {

    public static PermessoResponse from(Permesso p) {
        return new PermessoResponse(
                p.getId(), p.getUserId(), p.getTipo(),
                p.getDal(), p.getAl(),
                p.getOraInizio(), p.getOraFine(),
                p.getNote(), p.getCertificatoUrl(),
                p.getStato().name());
    }
}
