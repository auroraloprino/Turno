package com.turno.chat;

import java.time.Instant;
import java.util.List;

public class ChatDto {

    public record MessaggioResponse(
            Long id,
            Long conversazioneId,
            Long senderId,
            String senderName,
            String testo,
            String allegatoUrl,
            String allegatoNome,
            Instant timestamp
    ) {
        public static MessaggioResponse from(Messaggio m) {
            return new MessaggioResponse(
                    m.getId(),
                    m.getConversazione().getId(),
                    m.getSender().getId(),
                    m.getSender().getName(),
                    m.getTesto(),
                    m.getAllegatoUrl(),
                    m.getAllegatoNome(),
                    m.getTimestamp()
            );
        }
    }

    public record ConversazioneResponse(
            Long id,
            String tipo,
            String nome,
            List<Long> partecipanti
    ) {
        public static ConversazioneResponse from(Conversazione c) {
            return new ConversazioneResponse(
                    c.getId(),
                    c.getTipo(),
                    c.getNome(),
                    c.getPartecipanti().stream().map(u -> u.getId()).toList()
            );
        }
    }

    public record InviaMessaggioRequest(String testo) {}
}
