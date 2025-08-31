package com.example.taskmanager.board;

import com.example.taskmanager.board.dto.BoardDto;
import com.example.taskmanager.board.dto.BoardWithListsDto;
import com.example.taskmanager.board.dto.CreateBoardRequest;
import com.example.taskmanager.common.ForbiddenException;
import com.example.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserRepository users;
    private final com.example.taskmanager.security.BoardSecurity boardSecurity;

    @GetMapping
    public List<BoardDto> myBoards(Principal principal) {
        Long userId = resolveUserId(principal);
        return boardService.listMyBoards(userId);
    }

    @GetMapping("/{id}")
    public BoardWithListsDto getBoard(@PathVariable Long id, Authentication auth) {
        if (!boardSecurity.isMember(id, auth)) {
            throw new ForbiddenException("Not allowed");
        }
        return boardService.getBoardWithLists(id);
    }

    @PostMapping
    public BoardDto create(Principal principal, @RequestBody CreateBoardRequest req) {
        Long userId = resolveUserId(principal);
        return boardService.createBoard(userId, req);
    }

    /** Rename/update board name (owner only). */
    @PutMapping("/{id}")
    public BoardDto rename(@PathVariable Long id,
                           @RequestBody CreateBoardRequest req,
                           Principal principal) {
        if (req == null || req.name() == null || req.name().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name is required");
        }
        Long currentUserId = resolveUserId(principal);
        Board updated = boardService.update(id, req.name().trim(), currentUserId);
        return BoardMapper.toBoardDto(updated);
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String email = principal.getName(); // set by Spring Security from JWT
        return users.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}