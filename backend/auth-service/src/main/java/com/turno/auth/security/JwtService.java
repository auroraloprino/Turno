package com.turno.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtDecoder decoder;

    // i token li firma Keycloak, qui verifico solo la firma con la sua chiave pubblica
    public JwtService(@Value("${keycloak.url}") String keycloakUrl, @Value("${keycloak.realm}") String realm) {
        this.decoder = NimbusJwtDecoder
                .withJwkSetUri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs")
                .build();
    }

    public String email(String token) {
        return decoder.decode(token).getClaimAsString("email");
    }
}
