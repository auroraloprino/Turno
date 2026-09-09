package com.turno.user;

import com.turno.user.dto.ChangePasswordRequest;
import com.turno.user.dto.CreateUserRequest;
import com.turno.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già in uso");

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public void changePassword(Long userId, ChangePasswordRequest request, User caller) {
        if (!caller.getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        if (!passwordEncoder.matches(request.currentPassword(), caller.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password attuale errata");

        caller.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(caller);
    }

    public void delete(Long targetId, User caller) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // ADMIN can only delete USER
        if (caller.getRole() == Role.ADMIN && target.getRole() != Role.USER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        // nobody can delete themselves
        if (caller.getId().equals(targetId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        userRepository.delete(target);
    }
}
