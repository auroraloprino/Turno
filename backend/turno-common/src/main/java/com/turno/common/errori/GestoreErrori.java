package com.turno.common.errori;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// stesso formato {"message": ...} per tutti i servizi, il frontend legge sempre quel campo
@RestControllerAdvice
public class GestoreErrori {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> stato(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() != null ? e.getReason() : e.getStatusCode().toString()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validazione(MethodArgumentNotValidException e) {
        Map<String, String> campi = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "non valido",
                        (primo, secondo) -> primo));
        return ResponseEntity.badRequest().body(Map.of("message", "Dati non validi", "fields", campi));
    }

    // per esempio indice unico sull'email
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflitto(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Risorsa già esistente"));
    }
}
