package com.turno.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public User() {}

    public User(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Role role;

        public Builder id(Long id)
        { this.id = id; return this; }
        public Builder name(String name)
        { this.name = name; return this; }
        public Builder email(String email)
        { this.email = email; return this; }
        public Builder password(String pw)
        { this.password = pw; return this; }
        public Builder role(Role role)
        { this.role = role; return this; }
        public User build()
        { return new User(id, name, email, password, role); }
    }

    public Long getId()
    { return id; }
    public String getName()
    { return name; }
    public String getEmail()
    { return email; }
    public Role getRole()
    { return role; }

    public void setPassword(String password) 
    { this.password = password; }

    @Override public String getPassword()
    { return password; }
    @Override public String getUsername()
    { return email; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public boolean isAccountNonExpired()
    { return true; }
    @Override public boolean isAccountNonLocked()
    { return true; }
    @Override public boolean isCredentialsNonExpired() 
    { return true; }
    @Override public boolean isEnabled() 
    { return true; }
}
