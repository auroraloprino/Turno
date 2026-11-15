package com.turno.timbratura;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.turno.timbratura.security.AuthUser;

@RestController
@RequestMapping("/api/timbrature")
public class TimbraturaController {

    private final TimbraturaService service;

    public TimbraturaController(TimbraturaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TimbraturaResponse> timbra(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.timbra(user.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TimbraturaResponse>> storico(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.storico(user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<TimbraturaResponse>> tutte() {
        return ResponseEntity.ok(service.tutte());
    }
}
