package com.example.taskmanager.auth.dto;

import com.example.taskmanager.user.Role;

/** Returned after successful login/sign-up. */
public record AuthResponse(
        Long userId,
        String email,
        String name,
        Role role,
        String token
) {}