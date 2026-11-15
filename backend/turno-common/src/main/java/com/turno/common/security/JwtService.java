package com.turno.common.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class JwtService {

    private static final List<String> RUOLI = List.of("OWNER", "ADMIN", "USER", "SERVICE");

    private final JwtDecoder decoder;

    // il token lo firma Keycloak con RS256, serve solo la sua chiave pubblica (JWKS)
    public JwtService(String keycloakUrl, String realm) {
        // il JWKS viene scaricato al primo token, quindi Keycloak può partire anche dopo
        this.decoder = NimbusJwtDecoder
                .withJwkSetUri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs")
                .build();
    }

    public AuthUser toAuthUser(String token) {
        Jwt jwt = decoder.decode(token);
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        return new AuthUser(idNumerico(username), email != null ? email : username, ruolo(jwt));
    }

    // per gli utenti lo username in Keycloak è l'id numerico, per gli account di servizio no
    private static Long idNumerico(String username) {
        try {
            return Long.valueOf(username);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String ruolo(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Object ruoli = realmAccess != null ? realmAccess.get("roles") : null;
        if (ruoli instanceof Collection<?> c)
            for (String r : RUOLI)
                if (c.contains(r))
                    return r;
        throw new IllegalArgumentException("token senza un ruolo dell'applicazione");
    }
}
