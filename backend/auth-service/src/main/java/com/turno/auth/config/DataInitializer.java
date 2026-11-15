package com.turno.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.turno.auth.keycloak.KeycloakClient;
import com.turno.auth.user.Role;
import com.turno.auth.user.User;
import com.turno.auth.user.UserRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final KeycloakClient keycloak;

    @Value("${app.owner.email}")
    private String email;

    @Value("${app.owner.password}")
    private String rawPassword;

    public DataInitializer(UserRepository userRepository, KeycloakClient keycloak) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
    }

    @Override
    public void run(ApplicationArguments args) {
        User owner = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new User("Owner", email, Role.OWNER)));
        if (owner.getKeycloakId() != null)
            return;
        // owner già su Mongo ma non ancora su Keycloak, succede col primo avvio dopo il passaggio a Keycloak
        String keycloakId = keycloak.trovaPerEmail(email)
                .orElseGet(() -> keycloak.creaUtente(owner.getId(), owner.getName(), email, rawPassword, Role.OWNER.name()));
        // se un avvio precedente si è fermato a metà l'utente esiste ma senza ruolo
        keycloak.assegnaRuolo(keycloakId, Role.OWNER.name());
        owner.setKeycloakId(keycloakId);
        userRepository.save(owner);
    }
}
