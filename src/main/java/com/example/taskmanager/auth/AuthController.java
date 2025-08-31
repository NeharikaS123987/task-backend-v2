package com.example.taskmanager.auth;

import com.example.taskmanager.auth.dto.AuthResponse;
import com.example.taskmanager.auth.dto.LoginRequest;
import com.example.taskmanager.auth.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody @Valid SignupRequest req) {
        return auth.signup(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        return auth.login(req);
    }

    /** Returns the authenticated user's profile (no token in response). */
    @GetMapping("/me")
    public AuthResponse me(Principal principal) {
        if (principal == null) {
            // If no JWT / invalid JWT, surface a proper 401 instead of a 500
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return auth.me(principal.getName());
    }
}