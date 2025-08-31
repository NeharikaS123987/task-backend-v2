package com.example.taskmanager.task;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TaskSpecifications {
    private TaskSpecifications() {}

    public static Specification<Task> inBoard(Long boardId) {
        return (root, q, cb) -> cb.equal(root.join("list").join("board").get("id"), boardId);
    }

    public static Specification<Task> titleOrDescriptionContains(String qstr) {
        if (qstr == null || qstr.isBlank()) return null;
        String like = "%" + qstr.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> assignedTo(Long userId) {
        if (userId == null) return null;
        return (root, q, cb) -> cb.equal(root.join("assignees").get("id"), userId);
    }

    public static Specification<Task> dueBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            if (from != null && to != null) return cb.between(root.get("dueDate"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("dueDate"), from);
            return cb.lessThanOrEqualTo(root.get("dueDate"), to);
        };
    }
}