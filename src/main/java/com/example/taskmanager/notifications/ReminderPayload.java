package com.example.taskmanager.notifications;

import java.time.LocalDate;

/** Info included in reminder emails. */
public record ReminderPayload(
        Long taskId,
        String taskTitle,
        LocalDate dueDate,
        Long boardId,
        String boardName
) { }