package com.example.taskmanager.task.events;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

/** Published when task assignees change. */
public class TaskAssignedEvent extends ApplicationEvent {
    public final Long taskId;
    public final Long boardId;
    public final String taskTitle;
    public final Set<String> assigneeEmails;

    public TaskAssignedEvent(Object src, Long taskId, Long boardId, String taskTitle, Set<String> assigneeEmails) {
        super(src);
        this.taskId = taskId;
        this.boardId = boardId;
        this.taskTitle = taskTitle;
        this.assigneeEmails = assigneeEmails;
    }
}