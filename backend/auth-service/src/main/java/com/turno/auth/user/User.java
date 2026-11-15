package com.turno.auth.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.turno.common.mongo.ConIdNumerico;

@Document(collection = "users")
public class User implements ConIdNumerico {

    @Id
    private Long id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private Role role;

    private String team;

    // la password vive solo in Keycloak, qui tengo il collegamento all'utente di Keycloak
    private String keycloakId;

    public User() {}

    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId()   { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public Role getRole()    { return role; }
    public String getTeam()  { return team; }
    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }
    // stringa vuota o solo spazi = no team
    public void setTeam(String team) {
        this.team = (team == null || team.isBlank()) ? null : team.trim();
    }
}
