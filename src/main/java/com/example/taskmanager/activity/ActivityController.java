package com.example.taskmanager.activity;

import com.example.taskmanager.security.BoardSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/activity")
public class ActivityController {

    private final ActivityLogService service;
    private final BoardSecurity boardSecurity;

    /**
     * Paginated feed for a board. Default: page=0, size=20.
     * Requires board membership (or ADMIN).
     */
    @GetMapping
    @PreAuthorize("@boardSecurity.isMember(#boardId, authentication)")
    public Page<ActivityLog> list(@PathVariable Long boardId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        int sz = Math.min(Math.max(size, 1), 100); // clamp 1..100
        Pageable pageable = PageRequest.of(Math.max(page, 0), sz);
        return service.forBoard(boardId, pageable);
    }

    /**
     * Admin-only: purge activity for a board (rarely used, but useful for tests/resets).
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void purge(@PathVariable Long boardId) {
        service.deleteAllForBoard(boardId);
    }
}