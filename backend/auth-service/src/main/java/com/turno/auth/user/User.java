package com.turno.auth.user;

import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.turno.auth.mongo.ConIdNumerico;

@Document(collection = "users")
public class User implements UserDetails, ConIdNumerico {

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

    @Override public String getPassword()  { return null; }
    @Override public String getUsername()  { return email; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
