package com.example.taskmanager.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sign-up payload. Role defaults to USER server-side. */
public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        @Size(max = 100) String name
) {}