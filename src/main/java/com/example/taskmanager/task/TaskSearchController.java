package com.example.taskmanager.task;

import com.example.taskmanager.task.dto.TaskSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class TaskSearchController {

    private final TaskService svc;

    // Original: /api/tasks/search?boardId=...
    @GetMapping("/api/tasks/search")
    @PreAuthorize("@boardSecurity.isMember(#boardId, authentication)")
    public Page<TaskSummaryDto> searchParam(@RequestParam Long boardId,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) TaskStatus status,
                                            @RequestParam(required = false) Long assigneeId,
                                            @RequestParam(required = false) LocalDate from,
                                            @RequestParam(required = false) LocalDate to,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return svc.search(boardId, q, status, assigneeId, from, to, pageable);
    }

    // NEW: board-scoped path to match your calls: /api/boards/{boardId}/tasks/search
    @GetMapping("/api/boards/{boardId}/tasks/search")
    @PreAuthorize("@boardSecurity.isMember(#boardId, authentication)")
    public Page<TaskSummaryDto> searchPath(@PathVariable Long boardId,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false) TaskStatus status,
                                           @RequestParam(required = false) Long assigneeId,
                                           @RequestParam(required = false) LocalDate from,
                                           @RequestParam(required = false) LocalDate to,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return svc.search(boardId, q, status, assigneeId, from, to, pageable);
    }
}