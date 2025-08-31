package com.example.taskmanager.security;

import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Helper for resolving current user entity or id from Authentication. */
@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserRepository users;

    public Long currentUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return users.findByEmail(auth.getName()).map(User::getId).orElse(null);
    }

    public User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return users.findByEmail(auth.getName()).orElse(null);
    }
}