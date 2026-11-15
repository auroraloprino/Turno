package com.turno.orchestratore.clients;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class TokenServizio {

    private final RestClient http;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    private String token;
    private Instant scadenza = Instant.EPOCH;

    public TokenServizio(RestClient.Builder builder,
            @Value("${keycloak.url}") String url,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret) {
        this.http = builder.baseUrl(url).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // client credentials, l'orchestratore entra con il suo account di servizio che ha solo il ruolo SERVICE
    public synchronized String get() {
        if (Instant.now().isBefore(scadenza))
            return token;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        Map<String, Object> risposta = http.post().uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() {});
        token = (String) risposta.get("access_token");
        long durata = ((Number) risposta.get("expires_in")).longValue();
        scadenza = Instant.now().plusSeconds(Math.max(durata - 30, 10));
        return token;
    }
}
