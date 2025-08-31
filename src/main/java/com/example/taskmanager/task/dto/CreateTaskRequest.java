package com.example.taskmanager.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 5000) String description,
        LocalDate dueDate
) {}