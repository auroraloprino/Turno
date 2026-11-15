package com.turno.chat;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turno.common.security.AuthUser;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @GetMapping("/conversazioni")
    public ResponseEntity<List<ChatDto.ConversazioneResponse>> conversazioni(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.conversazioni(user.getId()));
    }

    @GetMapping("/conversazioni/{id}/messaggi")
    public ResponseEntity<List<ChatDto.MessaggioResponse>> messaggi(
            @PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.messaggi(id, user.getId()));
    }

    @PostMapping("/conversazioni/{id}/messaggi")
    public ResponseEntity<ChatDto.MessaggioResponse> invia(
            @PathVariable Long id,
            @Valid @RequestBody ChatDto.InviaMessaggioRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.invia(id, request.testo(), user));
    }

    @PatchMapping("/messaggi/{msgId}")
    public ResponseEntity<ChatDto.MessaggioResponse> modifica(
            @PathVariable Long msgId,
            @RequestBody ChatDto.ModificaMessaggioRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.modifica(msgId, request.testo(), user.getId()));
    }

    @DeleteMapping("/messaggi/{msgId}")
    public ResponseEntity<Void> elimina(
            @PathVariable Long msgId, @AuthenticationPrincipal AuthUser user) {
        service.elimina(msgId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversazioni/gruppo")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ChatDto.ConversazioneResponse> creaGruppo(@RequestBody CreaGruppoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creaGruppo(request.nome(), request.userIds()));
    }

    @PostMapping("/conversazioni/privata")
    public ResponseEntity<ChatDto.ConversazioneResponse> creaPrivata(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam Long conUserId,
            @RequestParam String nomeUtente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creaPrivata(user.getId(), conUserId, nomeUtente));
    }

    // la chiama solo l'orchestratore quando un utente viene cancellato, ripeterla non fa danni
    @DeleteMapping("/utente/{userId}")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<Void> eliminaPerUtente(@PathVariable Long userId) {
        service.eliminaPerUtente(userId);
        return ResponseEntity.noContent().build();
    }

    public record CreaGruppoRequest(@NotBlank String nome, @NotEmpty List<Long> userIds) {
    }
}
