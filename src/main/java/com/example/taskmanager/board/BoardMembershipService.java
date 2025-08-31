package com.example.taskmanager.board;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardMembershipService {

    private final BoardService boards;

    public List<BoardMember> list(Long boardId) {
        return boards.membersOf(boardId);
    }

    public BoardMember invite(Long boardId, Long userId, BoardMemberRole role, Authentication auth) {
        return boards.invite(boardId, userId, role, auth);
    }

    public BoardMember updateRole(Long boardId, Long userId, BoardMemberRole role, Authentication auth) {
        return boards.updateRole(boardId, userId, role, auth);
    }

    public void remove(Long boardId, Long userId, Authentication auth) {
        boards.remove(boardId, userId, auth);
    }
}