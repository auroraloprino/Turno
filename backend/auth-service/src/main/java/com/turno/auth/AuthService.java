package com.turno.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.turno.auth.dto.LoginRequest;
import com.turno.auth.dto.LoginResponse;
import com.turno.auth.keycloak.KeycloakClient;
import com.turno.auth.user.User;
import com.turno.auth.user.UserRepository;

@Service
public class AuthService {

    private final KeycloakClient keycloak;
    private final UserRepository userRepository;

    public AuthService(KeycloakClient keycloak, UserRepository userRepository) {
        this.keycloak = keycloak;
        this.userRepository = userRepository;
    }

    // il token lo rilascia Keycloak, io aggiungo solo i dati del profilo che servono al frontend
    public LoginResponse login(LoginRequest request) {
        String token = keycloak.login(request.email(), request.password());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenziali non valide"));
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
