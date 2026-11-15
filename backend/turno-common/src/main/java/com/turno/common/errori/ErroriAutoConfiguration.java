package com.turno.common.errori;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(GestoreErrori.class)
public class ErroriAutoConfiguration {
}
