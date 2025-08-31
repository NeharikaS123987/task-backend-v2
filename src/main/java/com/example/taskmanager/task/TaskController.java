package com.example.taskmanager.task;

import com.example.taskmanager.security.AuthUtils;
import com.example.taskmanager.security.BoardSecurity;
import com.example.taskmanager.task.dto.CreateTaskRequest;
import com.example.taskmanager.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TaskController {

    private final TaskService svc;
    private final BoardSecurity boardSecurity; // reserved for future checks
    private final AuthUtils authUtils;

    /* ---------- List-scoped endpoints (existing) ---------- */

    @GetMapping("/lists/{listId}/tasks")
    public List<Task> byList(@PathVariable Long listId, Authentication auth) {
        // membership can be validated inside service via BoardSecurity if desired
        return svc.byList(listId);
    }

    @PostMapping("/lists/{listId}/tasks")
    public Task create(@PathVariable Long listId,
                       @RequestBody @Valid CreateTaskRequest req,
                       Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        return svc.create(listId, req.title(), req.description(), req.dueDate(), actorId);
    }

    @PutMapping("/lists/{listId}/tasks/{taskId}")
    public Task updatePut(@PathVariable Long listId,
                          @PathVariable Long taskId,
                          @RequestBody UpdateTaskRequest req,
                          Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        return svc.update(taskId, req.title(), req.description(), req.dueDate(), req.status(), actorId);
    }

    @DeleteMapping("/lists/{listId}/tasks/{taskId}")
    public void delete(@PathVariable Long listId,
                       @PathVariable Long taskId,
                       Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        svc.delete(taskId, actorId);
    }

    /* ---------- Task-scoped PATCH ----------
       Allows PATCH /api/tasks/{taskId}
       Body fields are optional and only provided ones are updated.
    */
    @PatchMapping("/tasks/{taskId}")
    public Task patch(@PathVariable Long taskId,
                      @RequestBody UpdateTaskRequest req,
                      Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        return svc.update(taskId, req.title(), req.description(), req.dueDate(), req.status(), actorId);
    }

    /* ---------- Assignees (new) ----------
       Replaces the set of assignees: PUT /api/tasks/{taskId}/assignees
       Body example: {"userIds":[1,2]}
    */
    @PutMapping("/tasks/{taskId}/assignees")
    public Task replaceAssignees(@PathVariable Long taskId,
                                 @RequestBody AssignRequest req,
                                 Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        return svc.assign(taskId, req.userIds(), actorId);
    }

    /** Minimal request body for assignee replacement. */
    public record AssignRequest(Set<Long> userIds) {}
}