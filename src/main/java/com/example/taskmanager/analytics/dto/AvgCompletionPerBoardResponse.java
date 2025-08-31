package com.example.taskmanager.analytics.dto;

/** Average task completion time per board (in hours, fractional). */
public record AvgCompletionPerBoardResponse(
        Long boardId,
        String boardName,
        double avgHours
) {}