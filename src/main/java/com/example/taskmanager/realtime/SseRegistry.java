package com.example.taskmanager.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Keeps track of active SSE emitters (subscribers) per board and
 * offers helper methods to broadcast events.
 */
@Component
@Slf4j
public class SseRegistry {

    /** Emitters per boardId. CopyOnWriteArrayList is safe for concurrent iteration. */
    private final ConcurrentMap<Long, CopyOnWriteArrayList<SseEmitter>> byBoard = new ConcurrentHashMap<>();

    /** Default timeout: 30 minutes. Clients should reconnect when closed. */
    public static final long DEFAULT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);

    /** Register a new emitter for a board. */
    public SseEmitter register(Long boardId) {
        var emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        byBoard.computeIfAbsent(boardId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(boardId, emitter));
        emitter.onTimeout(() -> remove(boardId, emitter));
        emitter.onError(ex -> remove(boardId, emitter));

        // Send a small "connected" event so the client knows it's live
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("ts", Instant.now().toString(), "boardId", boardId))
                    .id(String.valueOf(System.currentTimeMillis()))
                    .reconnectTime(2_000L));
        } catch (IOException e) {
            remove(boardId, emitter);
        }
        return emitter;
    }

    /** Remove a completed/failed emitter. */
    private void remove(Long boardId, SseEmitter emitter) {
        var list = byBoard.get(boardId);
        if (list == null) return;
        list.remove(emitter);
        if (list.isEmpty()) byBoard.remove(boardId, list);
    }

    /** Broadcast an event (JSON data) to all subscribers on a board. */
    public void sendToBoard(Long boardId, String eventName, Object payload) {
        var list = byBoard.get(boardId);
        if (list == null || list.isEmpty()) return;

        String id = String.valueOf(System.currentTimeMillis());
        for (SseEmitter emitter : List.copyOf(list)) {
            try {
                emitter.send(SseEmitter.event()
                        .name(Objects.requireNonNullElse(eventName, "message"))
                        .id(id)
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                remove(boardId, emitter);
            }
        }
    }

    /** Number of active subscribers for a board (useful for debugging/metrics). */
    public int subscribers(Long boardId) {
        var list = byBoard.get(boardId);
        return list == null ? 0 : list.size();
    }
}