package com.example.taskmanager.list.dto;

public record UpdateListRequest(
        String name,
        Integer position
) {}