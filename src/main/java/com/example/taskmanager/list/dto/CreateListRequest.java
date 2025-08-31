package com.example.taskmanager.list.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateListRequest(
        @NotBlank String name,
        Integer position
) {}