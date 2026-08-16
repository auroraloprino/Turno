package com.turno.chat;

import com.turno.user.User;
import com.turno.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class ChatService {

    private final ConversazioneRepository convRepo;
    private final MessaggioRepository msgRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate broker;

    public ChatService(ConversazioneRepository convRepo, MessaggioRepository msgRepo,
                       UserRepository userRepo, SimpMessagingTemplate broker) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
        this.broker = broker;
    }

    public List<ChatDto.ConversazioneResponse> conversazioni(Long userId) {
        return convRepo.findByPartecipanteId(userId)
                .stream().map(ChatDto.ConversazioneResponse::from).toList();
    }

    public List<ChatDto.MessaggioResponse> messaggi(Long conversazioneId, Long userId) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean partecipa = conv.getPartecipanti().stream().anyMatch(u -> u.getId().equals(userId));
        if (!partecipa) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return msgRepo.findByConversazioneIdOrderByTimestampAsc(conversazioneId)
                .stream().map(ChatDto.MessaggioResponse::from).toList();
    }

    public ChatDto.MessaggioResponse invia(Long conversazioneId, String testo, User sender) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean partecipa = conv.getPartecipanti().stream().anyMatch(u -> u.getId().equals(sender.getId()));
        if (!partecipa) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        Messaggio m = msgRepo.save(new Messaggio(conv, sender, testo, Instant.now()));
        ChatDto.MessaggioResponse response = ChatDto.MessaggioResponse.from(m);
        broker.convertAndSend("/topic/conversazione/" + conversazioneId, response);
        return response;
    }

    public ChatDto.ConversazioneResponse creaGruppo(String nome, List<Long> userIds) {
        Conversazione conv = new Conversazione();
        conv.setTipo("group");
        conv.setNome(nome);
        conv.setPartecipanti(Set.copyOf(userRepo.findAllById(userIds)));
        return ChatDto.ConversazioneResponse.from(convRepo.save(conv));
    }

    public ChatDto.ConversazioneResponse creaPrivata(Long userIdA, Long userIdB) {
        User a = userRepo.findById(userIdA).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User b = userRepo.findById(userIdB).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Conversazione conv = new Conversazione();
        conv.setTipo("private");
        conv.setNome(b.getName());
        conv.setPartecipanti(Set.of(a, b));
        return ChatDto.ConversazioneResponse.from(convRepo.save(conv));
    }
}
