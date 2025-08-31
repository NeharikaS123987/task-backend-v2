package com.example.taskmanager.activity;

import com.example.taskmanager.board.Board;
import com.example.taskmanager.board.BoardRepository;
import com.example.taskmanager.common.NotFoundException;
import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activity;
    private final BoardRepository boards;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public Page<ActivityLog> forBoard(Long boardId, Pageable pageable) {
        // If board doesn't exist, return 404 instead of empty page
        boards.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));
        return activity.findByBoardIdOrderByCreatedAtDesc(boardId, pageable);
    }

    /** Convenience logger: actorId can be null for system events. */
    @Transactional
    public ActivityLog log(Long boardId, Long actorId, ActivityType type, String detail) {
        Board b = boards.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        User actor = null;
        if (actorId != null) {
            actor = users.findById(actorId).orElseThrow(() -> new NotFoundException("Actor not found"));
        }

        ActivityLog row = ActivityLog.builder()
                .board(b)
                .actor(actor)
                .type(type)
                .detail(detail)
                .build();
        return activity.save(row);
    }

    @Transactional
    public void deleteAllForBoard(Long boardId) {
        // ensure board exists first to give a useful error
        boards.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));
        activity.deleteByBoardId(boardId);
    }
}