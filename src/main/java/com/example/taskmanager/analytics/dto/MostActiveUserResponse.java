package com.example.taskmanager.analytics.dto;

public record MostActiveUserResponse(
        Long userId,
        String email,
        long activityCount
) {}