package com.turno.turni;

import java.util.List;

public record BatchResult(List<TurnoResponse> creati, int saltati) {
}
