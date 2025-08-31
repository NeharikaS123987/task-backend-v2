package com.example.taskmanager.task.dto;

import com.example.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(max = 255) String title,
        @Size(max = 5000) String description,
        LocalDate dueDate,
        TaskStatus status
) {}