package com.example.taskmanager.board.dto;

import com.example.taskmanager.board.BoardMember;
import com.example.taskmanager.board.BoardMemberRole;

public record BoardMemberResponse(
        Long userId,
        String email,
        String name,
        BoardMemberRole role
) {
    public static BoardMemberResponse from(BoardMember m) {
        var u = m.getUser();
        return new BoardMemberResponse(u.getId(), u.getEmail(), u.getName(), m.getRole());
    }
}