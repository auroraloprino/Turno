package com.turno.permesso;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PermessoRequest(
                @NotBlank String tipo,
                @NotNull LocalDate dal,
                @NotNull LocalDate al,
                LocalTime oraInizio,
                LocalTime oraFine,
                String note,
                String certificatoUrl) {
}
