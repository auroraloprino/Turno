package com.turno.chat.kafka;

public record PermessoStatoEvent(Long permessoId, Long userId, String stato) {
}
