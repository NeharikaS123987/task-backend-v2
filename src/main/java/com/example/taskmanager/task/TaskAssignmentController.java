package com.example.taskmanager.task;

import com.example.taskmanager.security.AuthUtils;
import com.example.taskmanager.task.dto.AssignUsersRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists/{listId}/tasks/{taskId}/assignees")
public class TaskAssignmentController {

    private final TaskService svc;
    private final AuthUtils authUtils;

    @PutMapping
    public Task assign(@PathVariable Long listId,
                       @PathVariable Long taskId,
                       @RequestBody AssignUsersRequest req,
                       org.springframework.security.core.Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        return svc.assign(taskId, req.userIds(), actorId);
    }
}