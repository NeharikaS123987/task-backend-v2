package com.example.taskmanager.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credentials for logging in. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}