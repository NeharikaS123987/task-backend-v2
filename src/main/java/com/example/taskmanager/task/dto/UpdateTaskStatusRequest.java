package com.example.taskmanager.task.dto;

import com.example.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {}