package com.example.taskmanager.auth;

import com.example.taskmanager.auth.dto.AuthResponse;
import com.example.taskmanager.auth.dto.LoginRequest;
import com.example.taskmanager.auth.dto.SignupRequest;
import com.example.taskmanager.common.BadRequestException;
import com.example.taskmanager.user.Role;
import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import com.example.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    @Transactional
    public AuthResponse signup(SignupRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        User u = User.builder()
                .email(email)
                .passwordHash(encoder.encode(req.password()))
                .name(req.name())
                .role(Role.USER) // default role
                .build();
        users.save(u);
        String token = jwt.generateToken(u.getEmail(), "ROLE_" + u.getRole().name());
        return new AuthResponse(u.getId(), u.getEmail(), u.getName(), u.getRole(), token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        // authenticate (will throw if invalid)
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User u = users.findByEmail(email).orElseThrow(); // should exist after auth
        String token = jwt.generateToken(u.getEmail(), "ROLE_" + u.getRole().name());
        return new AuthResponse(u.getId(), u.getEmail(), u.getName(), u.getRole(), token);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(String email) {
        User u = users.findByEmail(email).orElseThrow();
        return new AuthResponse(u.getId(), u.getEmail(), u.getName(), u.getRole(), null);
    }
}