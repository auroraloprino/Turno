package com.turno.timbratura.kafka;

import java.time.Instant;

public record TimbraturaAuditEvent(Long userId, String tipo, Instant timestamp) {
}
