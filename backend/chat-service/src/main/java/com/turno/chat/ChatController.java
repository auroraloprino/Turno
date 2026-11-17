package com.turno.chat;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping(value = "/conversazioni/{id}/immagini", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatDto.MessaggioResponse> inviaImmagine(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "testo", required = false) String testo,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inviaImmagine(id, file, testo, user));
    }

    @GetMapping("/immagini/{id}")
    public ResponseEntity<InputStreamResource> immagine(
            @PathVariable String id, @AuthenticationPrincipal AuthUser user) throws IOException {
        GridFsResource file = service.immagine(id, user);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                // l'immagine non cambia, il browser la tiene in cache
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate())
                .body(new InputStreamResource(file.getInputStream()));
    }

    @PostMapping("/conversazioni/gruppo")
    public ResponseEntity<ChatDto.ConversazioneResponse> creaGruppo(
            @Valid @RequestBody CreaGruppoRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creaGruppo(request.nome(), request.userIds(), user));
    }

    @PostMapping("/conversazioni/{id}/partecipanti")
    public ResponseEntity<ChatDto.ConversazioneResponse> aggiungi(
            @PathVariable Long id,
            @Valid @RequestBody AggiungiRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.aggiungi(id, request.userIds(), user));
    }

    @DeleteMapping("/conversazioni/{id}/partecipanti/me")
    public ResponseEntity<Void> abbandona(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        service.abbandona(id, user);
        return ResponseEntity.noContent().build();
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

    public record AggiungiRequest(@NotEmpty List<Long> userIds) {
    }

    public record CreaGruppoRequest(@NotBlank String nome, @NotEmpty List<Long> userIds) {
    }
}
