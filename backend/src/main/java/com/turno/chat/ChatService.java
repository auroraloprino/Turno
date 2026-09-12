package com.turno.chat;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.turno.user.User;
import com.turno.user.UserRepository;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ConversazioneRepository convRepo;
    private final MessaggioRepository msgRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate broker;
    private final MinioClient minio;

    @Value("${minio.bucket}")
    private String bucket;

    public ChatService(ConversazioneRepository convRepo, MessaggioRepository msgRepo,
                       UserRepository userRepo, SimpMessagingTemplate broker, MinioClient minio) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
        this.broker = broker;
        this.minio = minio;
    }

    public List<ChatDto.ConversazioneResponse> conversazioni(Long userId) {
        return convRepo.findByPartecipanteId(userId)
                .stream().map(c -> ChatDto.ConversazioneResponse.from(c, userId)).toList();
    }

    public List<ChatDto.MessaggioResponse> messaggi(Long conversazioneId, Long userId) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean partecipa = conv.getPartecipanti().stream().anyMatch(u -> u.getId().equals(userId));
        if (!partecipa) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return msgRepo.findByConversazioneIdOrderByTimestampAsc(conversazioneId)
                .stream().map(ChatDto.MessaggioResponse::from).toList();
    }

    @Transactional
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

    @Transactional
    public ChatDto.MessaggioResponse inviaConAllegato(Long conversazioneId, String testo, MultipartFile file, User sender) {
        Conversazione conv = convRepo.findById(conversazioneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean partecipa = conv.getPartecipanti().stream().anyMatch(u -> u.getId().equals(sender.getId()));
        if (!partecipa) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            minio.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload fallito");
        }

        String url;
        try {
            url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectName).method(Method.GET)
                    .expiry(7, TimeUnit.DAYS).build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "URL fallito");
        }

        Messaggio m = new Messaggio(conv, sender, testo.isBlank() ? file.getOriginalFilename() : testo, Instant.now());
        m.setAllegatoUrl(url);
        m.setAllegatoNome(file.getOriginalFilename());
        msgRepo.save(m);
        ChatDto.MessaggioResponse response = ChatDto.MessaggioResponse.from(m);
        broker.convertAndSend("/topic/conversazione/" + conversazioneId, response);
        return response;
    }

    @Transactional
    public ChatDto.MessaggioResponse modifica(Long messaggioId, String nuovoTesto, Long userId) {
        Messaggio m = msgRepo.findById(messaggioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!m.getSender().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.setTesto(nuovoTesto);
        m.setModificato(true);
        msgRepo.save(m);
        ChatDto.MessaggioResponse response = ChatDto.MessaggioResponse.from(m);
        broker.convertAndSend("/topic/conversazione/" + m.getConversazione().getId(), response);
        return response;
    }

    @Transactional
    public void elimina(Long messaggioId, Long userId) {
        Messaggio m = msgRepo.findById(messaggioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!m.getSender().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.setEliminato(true);
        msgRepo.save(m);
        broker.convertAndSend("/topic/conversazione/" + m.getConversazione().getId(),
                ChatDto.MessaggioResponse.from(m));
    }

    @Transactional
    public ChatDto.ConversazioneResponse creaGruppo(String nome, List<Long> userIds) {
        Conversazione conv = new Conversazione();
        conv.setTipo("group");
        conv.setNome(nome);
        conv.setPartecipanti(Set.copyOf(userRepo.findAllById(userIds)));
        return ChatDto.ConversazioneResponse.from(convRepo.save(conv));
    }

    @Transactional
    public ChatDto.ConversazioneResponse creaPrivata(Long userIdA, Long userIdB) {
        User a = userRepo.findById(userIdA).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User b = userRepo.findById(userIdB).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return convRepo.findByPartecipanteId(userIdA).stream()
                .filter(c -> c.getTipo().equals("private") &&
                        c.getPartecipanti().stream().anyMatch(u -> u.getId().equals(userIdB)))
                .findFirst()
                .map(ChatDto.ConversazioneResponse::from)
                .orElseGet(() -> {
                    Conversazione conv = new Conversazione();
                    conv.setTipo("private");
                    conv.setNome(b.getName());
                    conv.setPartecipanti(Set.of(a, b));
                    return ChatDto.ConversazioneResponse.from(convRepo.save(conv));
                });
    }
}
