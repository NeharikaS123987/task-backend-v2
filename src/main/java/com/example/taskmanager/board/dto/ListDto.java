package com.example.taskmanager.board.dto;

public record ListDto(
        Long id,
        String name,
        Integer position
) {}