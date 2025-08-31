package com.example.taskmanager.task.events;

import org.springframework.context.ApplicationEvent;

/** Generic task change (create/update/delete/status move). */
public class TaskChangedEvent extends ApplicationEvent {
    public final Long taskId;
    public final Long boardId;
    public final String type;

    public TaskChangedEvent(Object src, Long taskId, Long boardId, String type) {
        super(src);
        this.taskId = taskId;
        this.boardId = boardId;
        this.type = type;
    }
}