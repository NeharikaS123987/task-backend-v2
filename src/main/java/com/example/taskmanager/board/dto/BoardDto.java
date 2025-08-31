package com.example.taskmanager.board.dto;

import java.util.List;

public record BoardDto(
        Long id,
        String name,
        List<ListDto> lists
) {}