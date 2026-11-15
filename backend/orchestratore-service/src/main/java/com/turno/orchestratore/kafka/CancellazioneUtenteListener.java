package com.turno.orchestratore.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.turno.orchestratore.clients.ServiziClient;

@Component
public class CancellazioneUtenteListener {

    private static final Logger log = LoggerFactory.getLogger(CancellazioneUtenteListener.class);

    private final ServiziClient servizi;

    public CancellazioneUtenteListener(ServiziClient servizi) {
        this.servizi = servizi;
    }

    @KafkaListener(topics = "utente-cancellato", groupId = "orchestratore-group")
    public void onUtenteCancellato(UtenteCancellatoEvent evento) {
        log.info("Utente {} cancellato, pulisco i suoi dati negli altri servizi", evento.userId());
        for (String servizio : servizi.servizi()) {
            // se un servizio non risponde l'eccezione esce e Kafka riprova tutto il messaggio
            servizi.eliminaDatiUtente(servizio, evento.userId());
            log.info("Dati dell'utente {} eliminati da {}", evento.userId(), servizio);
        }
    }
}
