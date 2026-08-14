package com.turno.config;

import com.turno.user.Role;
import com.turno.user.User;
import com.turno.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.owner.email}")
    private String email;

    @Value("${app.owner.password}")
    private String rawPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(email)) return;

        userRepository.save(User.builder()
                .name("Owner")
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.OWNER)
                .build());
    }
}
