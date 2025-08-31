package com.example.taskmanager.analytics;

import com.example.taskmanager.analytics.dto.AvgCompletionPerBoardResponse;
import com.example.taskmanager.analytics.dto.BoardTaskCountsResponse;
import com.example.taskmanager.analytics.dto.MostActiveUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analytics;

    @GetMapping("/board-task-counts")
    public List<BoardTaskCountsResponse> boardTaskCounts() {
        return analytics.boardTaskCounts();
    }

    @GetMapping("/avg-completion-per-board")
    public List<AvgCompletionPerBoardResponse> avgCompletionPerBoard() {
        return analytics.avgCompletionPerBoard();
    }

    @GetMapping("/most-active-users")
    public List<MostActiveUserResponse> mostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        return analytics.mostActiveUsers(limit);
    }
}