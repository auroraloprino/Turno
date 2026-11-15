package com.turno.permessi;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.turno.permessi.security.AuthUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permessi")
public class PermessoController {

    private final PermessoService service;

    public PermessoController(PermessoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PermessoResponse> crea(
            @Valid @RequestBody PermessoRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crea(request, user));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PermessoResponse>> miei(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.miei(user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<PermessoResponse>> tutti() {
        return ResponseEntity.ok(service.tutti());
    }

    @GetMapping("/in-attesa")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<PermessoResponse>> inAttesa() {
        return ResponseEntity.ok(service.inAttesa());
    }

    @PatchMapping("/{id}/stato")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PermessoResponse> aggiorna(
            @PathVariable Long id,
            @RequestParam StatoPermesso stato) {
        return ResponseEntity.ok(service.aggiorna(id, stato));
    }

    // la chiama solo l'orchestratore quando un utente viene cancellato, ripeterla non fa danni
    @DeleteMapping("/utente/{userId}")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<Void> eliminaPerUtente(@PathVariable Long userId) {
        service.eliminaPerUtente(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PermessoResponse> modifica(
            @PathVariable Long id,
            @Valid @RequestBody PermessoRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.modifica(id, request, user));
    }
}
