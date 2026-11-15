package com.turno.chat;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.turno.common.security.AuthUser;

@Service
public class ChatService {

    private final ConversazioneRepository convRepo;
    private final MessaggioRepository msgRepo;
    private final ImmaginiService immagini;
    private final SimpMessagingTemplate broker;

    public ChatService(ConversazioneRepository convRepo, MessaggioRepository msgRepo, ImmaginiService immagini,
            SimpMessagingTemplate broker) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.immagini = immagini;
        this.broker = broker;
    }

    public List<ChatDto.ConversazioneResponse> conversazioni(Long userId) {
        return convRepo.findByPartecipanteId(userId)
                .stream().map(c -> ChatDto.ConversazioneResponse.from(c, userId)).toList();
    }

    public List<ChatDto.MessaggioResponse> messaggi(Long conversazioneId, Long userId) {
        Conversazione conv = partecipante(conversazioneId, userId);
        // il messaggio resta nel DB ma non viene mai restituito
        return msgRepo.findByConversazioneIdOrderByTimestampAsc(conv.getId())
                .stream().filter(m -> !m.isEliminato())
                .map(ChatDto.MessaggioResponse::from).toList();
    }

    public ChatDto.MessaggioResponse invia(Long conversazioneId, String testo, AuthUser sender) {
        if (testo == null || testo.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Messaggio vuoto");
        return pubblica(partecipante(conversazioneId, sender.getId()), testo, null, sender);
    }

    public ChatDto.MessaggioResponse inviaImmagine(Long conversazioneId, MultipartFile file, String testo,
            AuthUser sender) {
        Conversazione conv = partecipante(conversazioneId, sender.getId());
        String immagineId = immagini.salva(file, conv.getId());
        return pubblica(conv, testo == null ? "" : testo.trim(), immagineId, sender);
    }

    // l'immagine la vede solo chi partecipa alla conversazione in cui è stata mandata
    public GridFsResource immagine(String id, AuthUser utente) {
        partecipante(immagini.conversazioneDi(id), utente.getId());
        return immagini.leggi(id);
    }

    private ChatDto.MessaggioResponse pubblica(Conversazione conv, String testo, String immagineId, AuthUser sender) {
        Messaggio nuovo = new Messaggio(conv.getId(), sender.getId(), sender.getUsername(), testo, Instant.now());
        nuovo.setImmagineId(immagineId);
        ChatDto.MessaggioResponse response = ChatDto.MessaggioResponse.from(msgRepo.save(nuovo));
        broker.convertAndSend("/topic/conversazione/" + conv.getId(), response);
        String anteprima = testo.isBlank() ? "📷 immagine" : testo;
        conv.getPartecipantiIds().stream()
                .filter(id -> !id.equals(sender.getId()))
                .forEach(id -> broker.convertAndSend("/topic/notifiche/" + id,
                        Map.of("tipo", "chat", "conversazioneId", conv.getId(),
                                "messaggio", sender.getUsername() + ": " + anteprima)));
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

    // chiunque può creare un gruppo, chi lo crea ne fa parte in automatico
    public ChatDto.ConversazioneResponse creaGruppo(String nome, List<Long> userIds, AuthUser creatore) {
        Set<Long> partecipanti = new HashSet<>(userIds);
        partecipanti.add(creatore.getId());
        if (partecipanti.size() < 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un gruppo ha bisogno di almeno un'altra persona");
        Conversazione conv = new Conversazione();
        conv.setTipo("group");
        conv.setNome(nome.trim());
        conv.setPartecipantiIds(partecipanti);
        convRepo.save(conv);
        // gli altri membri non sanno ancora del gruppo, la notifica fa ricaricare le loro conversazioni
        partecipanti.stream()
                .filter(id -> !id.equals(creatore.getId()))
                .forEach(id -> broker.convertAndSend("/topic/notifiche/" + id,
                        Map.of("tipo", "gruppo", "conversazioneId", conv.getId(),
                                "messaggio", "Sei stato aggiunto al gruppo " + conv.getNome())));
        return ChatDto.ConversazioneResponse.from(conv, creatore.getId());
    }

    // le chat private con l'utente spariscono, dai gruppi viene solo tolto e i suoi messaggi restano leggibili
    public void eliminaPerUtente(Long userId) {
        List<Conversazione> sue = convRepo.findByPartecipanteId(userId);
        List<Long> private_ = sue.stream().filter(c -> c.getTipo().equals("private")).map(Conversazione::getId).toList();
        immagini.eliminaPerConversazioni(private_);
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

    private Conversazione partecipante(Long conversazioneId, Long userId) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!conv.getPartecipantiIds().contains(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return conv;
    }
}
