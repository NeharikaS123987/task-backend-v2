package com.example.taskmanager.task.dto;

import java.util.Set;

public record AssignUsersRequest(Set<Long> userIds) {}