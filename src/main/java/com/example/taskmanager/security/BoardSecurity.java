package com.example.taskmanager.security;

import com.example.taskmanager.board.BoardMemberRepository;
import com.example.taskmanager.board.BoardMemberRole;
import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class BoardSecurity {

    private final BoardMemberRepository members;
    private final UserRepository users;

    public BoardSecurity(BoardMemberRepository members, UserRepository users) {
        this.members = members;
        this.users = users;
    }

    private Long currentUserId(Authentication auth) {
        if (auth == null) return null;
        User u = users.findByEmail(auth.getName()).orElse(null);
        return u == null ? null : u.getId();
    }

    public boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isMember(Long boardId, Authentication auth) {
        if (isAdmin(auth)) return true;
        Long uid = currentUserId(auth);
        return uid != null && members.existsByBoardIdAndUserId(boardId, uid);
    }

    public boolean isOwner(Long boardId, Authentication auth) {
        if (isAdmin(auth)) return true;
        Long uid = currentUserId(auth);
        return uid != null && members.findByBoardIdAndUserId(boardId, uid)
                .map(m -> m.getRole() == BoardMemberRole.OWNER)
                .orElse(false);
    }

    /** Owner or Member can edit lists/tasks. */
    public boolean canEdit(Long boardId, Authentication auth) {
        if (isAdmin(auth)) return true;
        Long uid = currentUserId(auth);
        return uid != null && members.findByBoardIdAndUserId(boardId, uid)
                .map(m -> m.getRole() == BoardMemberRole.OWNER || m.getRole() == BoardMemberRole.MEMBER)
                .orElse(false);
    }
}