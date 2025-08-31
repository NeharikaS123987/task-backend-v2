package com.example.taskmanager.task.dto;

import com.example.taskmanager.task.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public record TaskSummaryDto(
        Long id,
        Long listId,
        Long boardId,
        String title,
        String description,
        LocalDate dueDate,
        TaskStatus status,
        Instant createdAt,
        Instant completedAt
) {}