package com.turno.orchestratore.clients;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ServiziClient {

    private final RestClient http;
    private final TokenServizio token;
    private final Map<String, String> endpoint = new LinkedHashMap<>();

    public ServiziClient(RestClient.Builder builder, TokenServizio token,
            @Value("${servizi.turni}") String turni,
            @Value("${servizi.permessi}") String permessi,
            @Value("${servizi.timbrature}") String timbrature,
            @Value("${servizi.chat}") String chat) {
        this.http = builder.build();
        this.token = token;
        endpoint.put("turni", turni + "/api/turni/utente/{id}");
        endpoint.put("permessi", permessi + "/api/permessi/utente/{id}");
        endpoint.put("timbrature", timbrature + "/api/timbrature/utente/{id}");
        endpoint.put("chat", chat + "/api/chat/utente/{id}");
    }

    public Iterable<String> servizi() {
        return endpoint.keySet();
    }

    // DELETE idempotente, se Kafka riconsegna il messaggio, rifarla non cambia niente
    public void eliminaDatiUtente(String servizio, Long userId) {
        http.delete().uri(endpoint.get(servizio), userId)
                .header("Authorization", "Bearer " + token.get())
                .retrieve().toBodilessEntity();
    }
}
