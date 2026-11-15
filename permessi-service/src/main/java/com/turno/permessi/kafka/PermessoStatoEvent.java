package com.turno.permessi.kafka;

public record PermessoStatoEvent(Long permessoId, Long userId, String stato) {
}
