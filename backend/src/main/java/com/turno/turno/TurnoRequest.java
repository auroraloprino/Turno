package com.turno.turno;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoRequest(
        @NotNull Long userId,
        @NotNull LocalDate data,
        @NotNull LocalTime inizio,
        @NotNull LocalTime fine
) {}
