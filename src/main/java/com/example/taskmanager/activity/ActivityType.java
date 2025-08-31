package com.example.taskmanager.activity;

/**
 * Short, stable event types for the activity feed.
 * Clients can key off these names.
 */
public enum ActivityType {
    BOARD_MEMBER_ADDED,
    BOARD_MEMBER_REMOVED,
    BOARD_UPDATED,
    LIST_CREATED,
    LIST_UPDATED,
    LIST_DELETED,
    TASK_CREATED,
    TASK_UPDATED,
    TASK_MOVED,
    TASK_DELETED,
    USER_JOINED_BOARD
}