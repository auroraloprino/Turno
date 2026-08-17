package com.turno.kafka;

import com.turno.timbratura.TipoTimbratura;
import java.time.Instant;

public record TimbraturaAuditEvent(Long userId, TipoTimbratura tipo, Instant timestamp) {}
