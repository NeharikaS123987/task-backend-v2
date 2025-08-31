package com.example.taskmanager.board.dto;

import com.example.taskmanager.board.BoardRole;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotNull Long userId,
        @NotNull BoardRole role
) { }