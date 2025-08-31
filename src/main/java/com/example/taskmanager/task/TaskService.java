package com.example.taskmanager.task;

import com.example.taskmanager.activity.ActivityLogService;
import com.example.taskmanager.activity.ActivityType;
import com.example.taskmanager.common.HtmlSanitizer;
import com.example.taskmanager.common.NotFoundException;
import com.example.taskmanager.list.BoardList;
import com.example.taskmanager.list.BoardListRepository;
import com.example.taskmanager.notifications.MailService;
import com.example.taskmanager.realtime.SseRegistry;
import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository tasks;
    private final BoardListRepository lists;
    private final UserRepository users;
    private final ActivityLogService activity;
    private final SseRegistry sse;
    private final MailService mail;

    @Transactional(readOnly = true)
    public List<Task> byList(Long listId) {
        ensureListExists(listId);
        return tasks.findByListId(listId);
    }

    @Transactional
    public Task create(Long listId, String title, String description, LocalDate dueDate, Long actorId) {
        BoardList list = ensureListExists(listId);
        Long boardId = list.getBoard().getId();

        String cleanTitle = HtmlSanitizer.sanitize(title);
        String cleanDesc  = HtmlSanitizer.sanitize(description);

        Task t = Task.builder()
                .title(cleanTitle)
                .description(cleanDesc)
                .dueDate(dueDate)
                .status(TaskStatus.TODO)
                .list(list)
                .build();
        t = tasks.save(t);

        try { activity.log(boardId, actorId, ActivityType.TASK_CREATED, "Task \"" + t.getTitle() + "\" created"); }
        catch (Exception e) { log.warn("Activity log failed after TASK_CREATED (taskId={}): {}", t.getId(), e.toString()); }
        try { sse.sendToBoard(boardId, "task-changed", Map.of("type","TASK_CREATED","taskId",t.getId())); }
        catch (Exception e) { log.warn("SSE notify failed after TASK_CREATED (taskId={}): {}", t.getId(), e.toString()); }
        return t;
    }

    @Transactional
    public Task update(Long taskId, String title, String description, LocalDate due, TaskStatus status, Long actorId) {
        Task t = tasks.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));

        BoardList list = t.getList();
        if (list == null || list.getBoard() == null) throw new NotFoundException("Task is not attached to a board");
        Long boardId = list.getBoard().getId();

        if (title != null)       t.setTitle(HtmlSanitizer.sanitize(title));
        if (description != null) t.setDescription(HtmlSanitizer.sanitize(description));
        if (due != null)         t.setDueDate(due);
        if (status != null) {
            t.setStatus(status);
            if (status == TaskStatus.DONE && t.getCompletedAt() == null) {
                t.setCompletedAt(Instant.now());
            } else if (status != TaskStatus.DONE && t.getCompletedAt() != null) {
                t.setCompletedAt(null);
            }
        }
        t = tasks.save(t);

        try { activity.log(boardId, actorId, ActivityType.TASK_UPDATED, "Task \"" + t.getTitle() + "\" updated"); }
        catch (Exception e) { log.warn("Activity log failed after TASK_UPDATED (taskId={}): {}", t.getId(), e.toString()); }
        try { sse.sendToBoard(boardId, "task-changed", Map.of("type","TASK_UPDATED","taskId",t.getId())); }
        catch (Exception e) { log.warn("SSE notify failed after TASK_UPDATED (taskId={}): {}", t.getId(), e.toString()); }
        return t;
    }

    @Transactional
    public void delete(Long taskId, Long actorId) {
        Task t = tasks.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));
        BoardList list = t.getList();
        if (list == null || list.getBoard() == null) { tasks.delete(t); return; }
        Long boardId = list.getBoard().getId();

        tasks.delete(t);

        try { activity.log(boardId, actorId, ActivityType.TASK_DELETED, "Task deleted"); }
        catch (Exception e) { log.warn("Activity log failed after TASK_DELETED (taskId={}): {}", taskId, e.toString()); }
        try { sse.sendToBoard(boardId, "task-changed", Map.of("type","TASK_DELETED","taskId",taskId)); }
        catch (Exception e) { log.warn("SSE notify failed after TASK_DELETED (taskId={}): {}", taskId, e.toString()); }
    }

    @Transactional
    public Task assign(Long taskId, Set<Long> userIds, Long actorId) {
        Task t = tasks.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));
        BoardList list = t.getList();
        if (list == null || list.getBoard() == null) throw new NotFoundException("Task is not attached to a board");
        Long boardId = list.getBoard().getId();

        List<User> assignees = users.findAllById(userIds == null ? Set.of() : userIds);
        t.getAssignees().clear();
        t.getAssignees().addAll(assignees);
        t = tasks.save(t);

        String boardName = list.getBoard().getName();
        for (User u : assignees) {
            try { mail.sendTaskAssigned(u.getEmail(), t.getTitle(), boardName); }
            catch (Exception e) { log.warn("Email notify failed for assignee={} taskId={}: {}", u.getEmail(), t.getId(), e.toString()); }
        }

        try { activity.log(boardId, actorId, ActivityType.TASK_UPDATED, "Task assigned/assignees replaced"); }
        catch (Exception e) { log.warn("Activity log failed after TASK_ASSIGNED (taskId={}): {}", t.getId(), e.toString()); }
        try { sse.sendToBoard(boardId, "task-changed", Map.of("type","TASK_ASSIGNED","taskId",t.getId())); }
        catch (Exception e) { log.warn("SSE notify failed after TASK_ASSIGNED (taskId={}): {}", t.getId(), e.toString()); }
        return t;
    }

    @Transactional
    public Task changeStatus(Long taskId, TaskStatus status, Long actorId) {
        return update(taskId, null, null, null, status, actorId);
    }

    @Transactional(readOnly = true)
    public Page<com.example.taskmanager.task.dto.TaskSummaryDto> search(Long boardId, String q, TaskStatus status,
                                                                        Long assigneeId, LocalDate from, LocalDate to,
                                                                        Pageable pageable) {
        List<Specification<Task>> specs = new ArrayList<>();
        specs.add(TaskSpecifications.inBoard(boardId));
        var s1 = TaskSpecifications.titleOrDescriptionContains(q);  if (s1 != null) specs.add(s1);
        var s2 = TaskSpecifications.hasStatus(status);              if (s2 != null) specs.add(s2);
        var s3 = TaskSpecifications.assignedTo(assigneeId);         if (s3 != null) specs.add(s3);
        var s4 = TaskSpecifications.dueBetween(from, to);           if (s4 != null) specs.add(s4);

        Specification<Task> spec = Specification.allOf(specs);
        return tasks.findAll(spec, pageable).map(t ->
                new com.example.taskmanager.task.dto.TaskSummaryDto(
                        t.getId(), t.getList().getId(), t.getList().getBoard().getId(),
                        t.getTitle(), t.getDescription(), t.getDueDate(), t.getStatus(),
                        t.getCreatedAt(), t.getCompletedAt()
                )
        );
    }

    private BoardList ensureListExists(Long listId) {
        return lists.findById(listId).orElseThrow(() -> new NotFoundException("List not found"));
    }
}