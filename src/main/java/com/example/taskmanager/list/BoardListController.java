package com.example.taskmanager.list;

import com.example.taskmanager.list.dto.CreateListRequest;
import com.example.taskmanager.list.dto.UpdateListRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/lists")
public class BoardListController {

    private final BoardListService svc;

    @GetMapping
    @PreAuthorize("@boardSecurity.isMember(#boardId, authentication)")
    public List<BoardList> byBoard(@PathVariable Long boardId, Authentication auth) {
        return svc.byBoard(boardId, auth);
    }

    @PostMapping
    @PreAuthorize("@boardSecurity.canEdit(#boardId, authentication)")
    public BoardList create(@PathVariable Long boardId,
                            @RequestBody @Valid CreateListRequest req,
                            Authentication auth) {
        return svc.create(boardId, req.name(), req.position(), auth);
    }

    @PutMapping("/{listId}")
    @PreAuthorize("@boardSecurity.canEdit(#boardId, authentication)")
    public BoardList update(@PathVariable Long boardId,
                            @PathVariable Long listId,
                            @RequestBody UpdateListRequest req,
                            Authentication auth) {
        return svc.update(listId, req.name(), req.position(), auth);
    }

    @DeleteMapping("/{listId}")
    @PreAuthorize("@boardSecurity.canEdit(#boardId, authentication)")
    public void delete(@PathVariable Long boardId,
                       @PathVariable Long listId,
                       Authentication auth) {
        svc.delete(listId, auth);
    }
}