package com.turno.kafka;

import com.turno.permesso.StatoPermesso;

public record PermessoStatoEvent(Long permessoId, Long userId, StatoPermesso stato) {}
