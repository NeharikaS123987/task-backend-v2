package com.example.taskmanager.realtime;

import com.example.taskmanager.security.BoardSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}")
public class StreamController {

    private final SseRegistry sse;
    private final BoardSecurity boardSecurity;

    /** Subscribe to board events (requires membership). */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    @PreAuthorize("@boardSecurity.isMember(#boardId, authentication)")
    public SseEmitter stream(@PathVariable Long boardId) {
        return sse.register(boardId);
    }
}