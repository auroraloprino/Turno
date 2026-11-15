package com.turno.common.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration
@EnableMethodSecurity
@EnableConfigurationProperties(SicurezzaProperties.class)
public class SicurezzaAutoConfiguration {

    // sempre liberi, /error va lasciato aperto altrimenti un 400 o 404 arriva al client come 401
    private static final List<String> SEMPRE_PUBBLICI = List.of("/actuator/health", "/error");

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(@Value("${keycloak.url}") String keycloakUrl, @Value("${keycloak.realm}") String realm) {
        return new JwtService(keycloakUrl, realm);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
        return new JwtAuthFilter(jwtService);
    }

    // senza questo Spring Boot registrerebbe il filtro una seconda volta fuori dalla catena di security
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterNonRegistrato(JwtAuthFilter filtro) {
        FilterRegistrationBean<JwtAuthFilter> registrazione = new FilterRegistrationBean<>(filtro);
        registrazione.setEnabled(false);
        return registrazione;
    }

    // ogni servizio aggiunge i suoi percorsi pubblici in application.yml, il resto vuole un token valido
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, SicurezzaProperties props)
            throws Exception {
        List<String> pubblici = new ArrayList<>(SEMPRE_PUBBLICI);
        pubblici.addAll(props.percorsiPubblici());
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(pubblici.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
