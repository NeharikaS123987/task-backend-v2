package com.example.taskmanager.board.dto;

import com.example.taskmanager.task.TaskStatus;
import java.util.List;

public record BoardWithListsDto(
        Long id,
        String name,
        Long ownerId,
        List<ListSummary> lists
) {
    public record ListSummary(
            Long id,
            String name,
            Integer position,
            List<TaskSummary> tasks
    ) {}

    public record TaskSummary(
            Long id,
            String title,
            TaskStatus status
    ) {}
}