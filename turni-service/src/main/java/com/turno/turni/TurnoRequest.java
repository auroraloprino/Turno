package com.turno.turni;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record TurnoRequest(
                @NotNull Long userId,
                @NotNull LocalDate data,
                @NotNull LocalTime inizio,
                @NotNull LocalTime fine) {
}
