package com.turno.chat;

import com.turno.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @GetMapping("/conversazioni")
    public ResponseEntity<List<ChatDto.ConversazioneResponse>> conversazioni(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.conversazioni(user.getId()));
    }

    @GetMapping("/conversazioni/{id}/messaggi")
    public ResponseEntity<List<ChatDto.MessaggioResponse>> messaggi(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.messaggi(id, user.getId()));
    }

    @PostMapping("/conversazioni/{id}/messaggi")
    public ResponseEntity<ChatDto.MessaggioResponse> invia(
            @PathVariable Long id,
            @Valid @RequestBody ChatDto.InviaMessaggioRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.invia(id, request.testo(), user));
    }

    @PostMapping(value = "/{id}/messaggi/allegato", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatDto.MessaggioResponse> inviaAllegato(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String testo,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.inviaConAllegato(id, testo, file, user));
    }

    @PostMapping("/conversazioni/gruppo")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ChatDto.ConversazioneResponse> creaGruppo(
            @RequestBody CreaGruppoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creaGruppo(request.nome(), request.userIds()));
    }

    @PostMapping("/conversazioni/privata")
    public ResponseEntity<ChatDto.ConversazioneResponse> creaPrivata(
            @AuthenticationPrincipal User user,
            @RequestParam Long conUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creaPrivata(user.getId(), conUserId));
    }

    public record CreaGruppoRequest(@NotBlank String nome, @NotEmpty List<Long> userIds) {}
}
