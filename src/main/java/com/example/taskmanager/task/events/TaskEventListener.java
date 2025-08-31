package com.example.taskmanager.task.events;

import com.example.taskmanager.notifications.MailService;
import com.example.taskmanager.realtime.SseRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final SseRegistry sse;
    private final MailService mail;

    @EventListener
    public void onChanged(TaskChangedEvent ev) {
        sse.sendToBoard(ev.boardId, "task-changed",
                java.util.Map.of("type", ev.type, "taskId", ev.taskId));
    }

    @EventListener
    public void onAssigned(TaskAssignedEvent ev) {
        // Already emailing in service, but if you prefer centralizing, you can do it here:
        // ev.assigneeEmails.forEach(e -> mail.sendTaskAssigned(e, ev.taskTitle, null));
        sse.sendToBoard(ev.boardId, "task-changed",
                java.util.Map.of("type", "TASK_ASSIGNED", "taskId", ev.taskId));
    }
}