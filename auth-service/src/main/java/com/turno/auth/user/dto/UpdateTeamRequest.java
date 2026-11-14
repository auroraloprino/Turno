package com.turno.auth.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateTeamRequest(@Size(max = 60) String team) {
}
