package com.turno.timbratura;

import com.turno.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timbrature")
public class TimbraturaController {

    private final TimbraturaService service;

    public TimbraturaController(TimbraturaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TimbraturaResponse> timbra(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.timbra(user));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TimbraturaResponse>> storico(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.storico(user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<TimbraturaResponse>> tutte() {
        return ResponseEntity.ok(service.tutте());
    }
}
