package com.example.taskmanager.analytics.dto;

public record BoardTaskCountsResponse(
        Long boardId,
        String boardName,
        long todo,
        long inProgress,
        long done
) {}