package com.turno.chat;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.turno.common.security.AuthUser;

@Service
public class ChatService {

    private final ConversazioneRepository convRepo;
    private final MessaggioRepository msgRepo;
    private final SimpMessagingTemplate broker;

    public ChatService(ConversazioneRepository convRepo, MessaggioRepository msgRepo,
            SimpMessagingTemplate broker) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.broker = broker;
    }

    public List<ChatDto.ConversazioneResponse> conversazioni(Long userId) {
        return convRepo.findByPartecipanteId(userId)
                .stream().map(c -> ChatDto.ConversazioneResponse.from(c, userId)).toList();
    }

    public List<ChatDto.MessaggioResponse> messaggi(Long conversazioneId, Long userId) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!conv.getPartecipantiIds().contains(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        // il messaggio resta nel DB ma non viene mai restituito
        return msgRepo.findByConversazioneIdOrderByTimestampAsc(conversazioneId)
                .stream().filter(m -> !m.isEliminato())
                .map(ChatDto.MessaggioResponse::from).toList();
    }

    public ChatDto.MessaggioResponse invia(Long conversazioneId, String testo, AuthUser sender) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!conv.getPartecipantiIds().contains(sender.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Messaggio m = msgRepo.save(new Messaggio(conv.getId(), sender.getId(), sender.getUsername(), testo, Instant.now()));
        ChatDto.MessaggioResponse response = ChatDto.MessaggioResponse.from(m);
        broker.convertAndSend("/topic/conversazione/" + conversazioneId, response);
        conv.getPartecipantiIds().stream()
                .filter(id -> !id.equals(sender.getId()))
                .forEach(id -> broker.convertAndSend("/topic/notifiche/" + id,
                        Map.of("tipo", "chat", "conversazioneId", conversazioneId,
                                "messaggio", sender.getUsername() + ": " + testo)));
        return response;
    }

    public ChatDto.MessaggioResponse modifica(Long messaggioId, String nuovoTesto, Long userId) {
        Messaggio m = msgRepo.findById(messaggioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!m.getSenderId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.setTesto(nuovoTesto);
        m.setModificato(true);
        msgRepo.save(m);
        ChatDto.MessaggioResponse response = ChatDto.MessaggioResponse.from(m);
        broker.convertAndSend("/topic/conversazione/" + m.getConversazioneId(), response);
        return response;
    }

    public void elimina(Long messaggioId, Long userId) {
        Messaggio m = msgRepo.findById(messaggioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!m.getSenderId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.setEliminato(true);
        msgRepo.save(m);
        broker.convertAndSend("/topic/conversazione/" + m.getConversazioneId(),
                ChatDto.MessaggioResponse.from(m));
    }

    public ChatDto.ConversazioneResponse creaGruppo(String nome, List<Long> userIds) {
        Conversazione conv = new Conversazione();
        conv.setTipo("group");
        conv.setNome(nome);
        conv.setPartecipantiIds(Set.copyOf(userIds));
        return ChatDto.ConversazioneResponse.from(convRepo.save(conv), null);
    }

    // le chat private con l'utente spariscono, dai gruppi viene solo tolto e i suoi messaggi restano leggibili
    public void eliminaPerUtente(Long userId) {
        List<Conversazione> sue = convRepo.findByPartecipanteId(userId);
        List<Long> private_ = sue.stream().filter(c -> c.getTipo().equals("private")).map(Conversazione::getId).toList();
        msgRepo.deleteByConversazioneIdIn(private_);
        convRepo.deleteAllById(private_);
        sue.stream().filter(c -> !c.getTipo().equals("private")).forEach(c -> {
            Set<Long> rimasti = new HashSet<>(c.getPartecipantiIds());
            rimasti.remove(userId);
            c.setPartecipantiIds(rimasti);
            convRepo.save(c);
        });
    }

    public ChatDto.ConversazioneResponse creaPrivata(Long userIdA, Long userIdB, String nomeB) {
        return convRepo.findByPartecipanteId(userIdA).stream()
                .filter(c -> c.getTipo().equals("private") && c.getPartecipantiIds().contains(userIdB))
                .findFirst()
                .map(c -> ChatDto.ConversazioneResponse.from(c, userIdA))
                .orElseGet(() -> {
                    Conversazione conv = new Conversazione();
                    conv.setTipo("private");
                    conv.setNome(nomeB);
                    conv.setPartecipantiIds(Set.of(userIdA, userIdB));
                    return ChatDto.ConversazioneResponse.from(convRepo.save(conv), userIdA);
                });
    }
}
