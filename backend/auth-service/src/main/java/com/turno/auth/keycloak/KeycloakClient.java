package com.turno.auth.keycloak;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class KeycloakClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAPPA = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA = new ParameterizedTypeReference<>() {};

    private final RestClient http;
    private final String url;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    private String tokenServizio;
    private Instant scadenzaTokenServizio = Instant.EPOCH;

    public KeycloakClient(RestClient.Builder builder,
            @Value("${keycloak.url}") String url,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret) {
        this.http = builder.baseUrl(url).build();
        this.url = url;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // login con il password grant del client pubblico, Keycloak risponde con il token firmato
    public String login(String email, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "turno-app");
        form.add("username", email);
        form.add("password", password);
        try {
            return (String) richiestaToken(form).get("access_token");
        } catch (HttpClientErrorException e) {
            // 401 per password sbagliata, 400 se l'account non è utilizzabile
            throw new BadCredentialsException("Credenziali non valide");
        }
    }

    // nello username metto l'id numerico, così finisce nel token come preferred_username
    public String creaUtente(Long id, String nome, String email, String password, String ruolo) {
        String[] parti = nome.trim().split("\\s+", 2);
        Map<String, Object> utente = Map.of(
                // Keycloak vuole almeno 3 caratteri, con gli zeri davanti Long.valueOf dà sempre lo stesso id
                "username", String.format("%06d", id),
                "email", email,
                "firstName", parti[0],
                // Keycloak vuole anche il cognome, altrimenti blocca il login chiedendo di completare il profilo
                "lastName", parti.length > 1 ? parti[1] : parti[0],
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credenziale(password)));
        URI location;
        try {
            location = admin().post().uri("/users").contentType(MediaType.APPLICATION_JSON).body(utente)
                    .retrieve().toBodilessEntity().getHeaders().getLocation();
        } catch (HttpClientErrorException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già in uso");
        }
        String path = location.getPath();
        String keycloakId = path.substring(path.lastIndexOf('/') + 1);
        assegnaRuolo(keycloakId, ruolo);
        return keycloakId;
    }

    public Optional<String> trovaPerEmail(String email) {
        List<Map<String, Object>> trovati = admin().get()
                .uri(u -> u.path("/users").queryParam("email", email).queryParam("exact", true).build())
                .retrieve().body(LISTA);
        return trovati == null || trovati.isEmpty() ? Optional.empty() : Optional.of((String) trovati.get(0).get("id"));
    }

    public void impostaPassword(String keycloakId, String password) {
        admin().put().uri("/users/{id}/reset-password", keycloakId)
                .contentType(MediaType.APPLICATION_JSON).body(credenziale(password))
                .retrieve().toBodilessEntity();
    }

    public void eliminaUtente(String keycloakId) {
        try {
            admin().delete().uri("/users/{id}", keycloakId).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ignored) {
            // già assente, va bene lo stesso
        }
    }

    // assegnare un ruolo già presente non dà errore, quindi si può chiamare più volte
    public void assegnaRuolo(String keycloakId, String ruolo) {
        Map<String, Object> rappresentazione = admin().get().uri("/roles/{nome}", ruolo).retrieve().body(MAPPA);
        admin().post().uri("/users/{id}/role-mappings/realm", keycloakId)
                .contentType(MediaType.APPLICATION_JSON).body(List.of(rappresentazione))
                .retrieve().toBodilessEntity();
    }

    private static Map<String, Object> credenziale(String password) {
        return Map.of("type", "password", "value", password, "temporary", false);
    }

    private RestClient admin() {
        return http.mutate()
                .baseUrl(url + "/admin/realms/" + realm)
                .defaultHeader("Authorization", "Bearer " + tokenServizio())
                .build();
    }

    // token dell'account di servizio turno-auth, lo riuso finché non sta per scadere
    private synchronized String tokenServizio() {
        if (Instant.now().isBefore(scadenzaTokenServizio))
            return tokenServizio;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        Map<String, Object> risposta = richiestaToken(form);
        tokenServizio = (String) risposta.get("access_token");
        long durata = ((Number) risposta.get("expires_in")).longValue();
        scadenzaTokenServizio = Instant.now().plusSeconds(Math.max(durata - 30, 10));
        return tokenServizio;
    }

    private Map<String, Object> richiestaToken(MultiValueMap<String, String> form) {
        return http.post().uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().body(MAPPA);
    }
}
