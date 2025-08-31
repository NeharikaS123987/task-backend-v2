package com.example.taskmanager.task;

import com.example.taskmanager.security.AuthUtils;
import com.example.taskmanager.task.dto.UpdateTaskStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists/{listId}/tasks/{taskId}/status")
public class TaskStatusController {

    private final TaskService svc;
    private final AuthUtils authUtils;

    @PutMapping
    public Task setStatus(@PathVariable Long listId,
                          @PathVariable Long taskId,
                          @RequestBody @Valid UpdateTaskStatusRequest req,
                          org.springframework.security.core.Authentication auth) {
        Long actorId = authUtils.currentUserId(auth);
        return svc.changeStatus(taskId, req.status(), actorId);
    }
}