package com.example.taskmanager.board;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/members")
@RequiredArgsConstructor
public class BoardMembershipController {

    private final BoardMembershipService membershipService;

    @GetMapping
    @PreAuthorize("@boardSecurity.isMember(#boardId, authentication)")
    public List<BoardMember> list(@PathVariable Long boardId) {
        return membershipService.list(boardId);
    }

    @PostMapping
    @PreAuthorize("@boardSecurity.isOwner(#boardId, authentication)")
    public BoardMember invite(@PathVariable Long boardId,
                              @RequestParam Long userId,
                              @RequestParam BoardMemberRole role,
                              Authentication auth) {
        return membershipService.invite(boardId, userId, role, auth);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("@boardSecurity.isOwner(#boardId, authentication)")
    public BoardMember updateRole(@PathVariable Long boardId,
                                  @PathVariable Long userId,
                                  @RequestParam BoardMemberRole role,
                                  Authentication auth) {
        return membershipService.updateRole(boardId, userId, role, auth);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("@boardSecurity.isOwner(#boardId, authentication)")
    public void remove(@PathVariable Long boardId,
                       @PathVariable Long userId,
                       Authentication auth) {
        membershipService.remove(boardId, userId, auth);
    }
}