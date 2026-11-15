package com.turno.auth.user;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.turno.auth.keycloak.KeycloakClient;
import com.turno.auth.outbox.Outbox;
import com.turno.auth.user.dto.ChangePasswordRequest;
import com.turno.auth.user.dto.CreateUserRequest;
import com.turno.auth.user.dto.UserResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakClient keycloak;
    private final Outbox outbox;
    private final TransactionTemplate transazione;

    public UserService(UserRepository userRepository, KeycloakClient keycloak, Outbox outbox,
            TransactionTemplate transazione) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
        this.outbox = outbox;
        this.transazione = transazione;
    }

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già in uso");
        User user = new User(request.name(), request.email(), request.role());
        user.setTeam(request.team());
        // salvo prima su Mongo per avere l'id numerico, che poi diventa lo username in Keycloak
        user = userRepository.save(user);
        try {
            user.setKeycloakId(keycloak.creaUtente(user.getId(), user.getName(), user.getEmail(),
                    request.password(), user.getRole().name()));
        } catch (RuntimeException e) {
            // se Keycloak rifiuta l'utente non lascio un account a metà su Mongo
            userRepository.delete(user);
            throw e;
        }
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse updateTeam(Long userId, String team) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        user.setTeam(team);
        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public void changePassword(Long userId, ChangePasswordRequest request, User caller) {
        if (!caller.getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        // la password attuale la verifico facendo un login vero su Keycloak
        try {
            keycloak.login(caller.getEmail(), request.currentPassword());
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password attuale errata");
        }
        keycloak.impostaPassword(caller.getKeycloakId(), request.newPassword());
    }

    public void delete(Long targetId, User caller) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (caller.getRole() == Role.ADMIN && target.getRole() != Role.USER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (caller.getId().equals(targetId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        // utente ed evento nella stessa transazione, poi l'orchestratore pulisce turni, permessi, timbrature e chat
        transazione.executeWithoutResult(tx -> {
            userRepository.delete(target);
            outbox.registra("utente-cancellato", targetId, Map.of("userId", targetId));
        });
        // Keycloak è fuori dalla transazione, se fallisse l'utente non potrebbe comunque più entrare perché manca su Mongo
        if (target.getKeycloakId() != null)
            keycloak.eliminaUtente(target.getKeycloakId());
    }
}
