package com.turno.chat;

import java.time.Instant;
import java.util.List;

public class ChatDto {

    public record MessaggioResponse(
            Long id, Long conversazioneId,
            Long senderId, String senderName,
            String testo,
            Instant timestamp, boolean modificato, boolean eliminato) {

        public static MessaggioResponse from(Messaggio m) {
            return new MessaggioResponse(
                    m.getId(), m.getConversazioneId(),
                    m.getSenderId(), m.getSenderName(),
                    m.getTesto(),
                    m.getTimestamp(), m.isModificato(), m.isEliminato());
        }
    }

    public record ConversazioneResponse(Long id, String tipo, String nome, List<Long> partecipanti) {
        public static ConversazioneResponse from(Conversazione c, Long viewerId) {
            String nome = c.getTipo().equals("private")
                    ? c.getNome()
                    : c.getNome();
            return new ConversazioneResponse(c.getId(), c.getTipo(), nome,
                    c.getPartecipantiIds().stream().toList());
        }
    }

    public record InviaMessaggioRequest(String testo) {
    }

    public record ModificaMessaggioRequest(String testo) {
    }
}
