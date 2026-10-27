package com.turno.kafka;

import java.time.Instant;

import com.turno.timbratura.TipoTimbratura;

public record TimbraturaAuditEvent(Long userId, TipoTimbratura tipo, Instant timestamp) {
}
