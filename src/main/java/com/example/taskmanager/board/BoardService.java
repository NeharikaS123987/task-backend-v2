package com.example.taskmanager.board;

import com.example.taskmanager.activity.ActivityLogService;
import com.example.taskmanager.activity.ActivityType;
import com.example.taskmanager.board.dto.BoardDto;
import com.example.taskmanager.board.dto.BoardWithListsDto;
import com.example.taskmanager.board.dto.CreateBoardRequest;
import com.example.taskmanager.common.NotFoundException;
import com.example.taskmanager.list.BoardListRepository;
import com.example.taskmanager.security.BoardSecurity;
import com.example.taskmanager.task.TaskRepository;
import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boards;
    private final BoardMemberRepository members;
    private final UserRepository users;
    private final BoardSecurity boardSecurity;

    private final ActivityLogService activity;
    private final TaskRepository tasks;
    private final BoardListRepository lists;

    private Long currentUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return users.findByEmail(auth.getName()).map(User::getId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Board get(Long boardId) {
        return boards.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));
    }

    @Transactional(readOnly = true)
    public List<BoardDto> listMyBoards(Long userId) {
        List<Board> userBoards = boards.findAllByMemberUserId(userId);
        userBoards.forEach(b -> Optional.ofNullable(b.getLists()).ifPresent(List::size));
        return userBoards.stream().map(BoardMapper::toBoardDto).toList();
    }

    @Transactional(readOnly = true)
    public BoardWithListsDto getBoardWithLists(Long boardId) {
        Board board = boards.findByIdWithLists(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if (board.getLists() != null && Hibernate.isInitialized(board.getLists())) {
            board.getLists().forEach(l -> {
                if (l.getTasks() != null && !Hibernate.isInitialized(l.getTasks())) {
                    l.getTasks().size(); // initialize tasks inside tx
                }
            });
        }
        return BoardMapper.toBoardWithListsDto(board);
    }

    @Transactional
    public Board create(String name, Long ownerId) {
        User u = users.findById(ownerId).orElseThrow(() -> new NotFoundException("Owner not found"));
        Board b = Board.builder().name(name).owner(u).build();
        boards.save(b);
        members.save(BoardMember.builder().board(b).user(u).role(BoardMemberRole.OWNER).build());
        return b;
    }

    @Transactional
    public BoardDto createBoard(Long currentUserId, CreateBoardRequest req) {
        User creator = users.getReferenceById(currentUserId);

        Board board = Board.builder()
                .name(req.name())
                .owner(creator)
                .build();
        board = boards.save(board);

        BoardMember member = BoardMember.builder()
                .board(board)
                .user(creator)
                .role(BoardMemberRole.OWNER)
                .build();
        members.save(member);

        board.getLists().size(); // init lists
        return BoardMapper.toBoardDto(board);
    }

    @Transactional
    public Board update(Long boardId, String name, Long currentUserId) {
        Board b = get(boardId);
        if (currentUserId == null || b.getOwner() == null || !b.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Only owner can rename board");
        }
        b.setName(name);
        Board saved = boards.save(b);

        try {
            activity.log(saved.getId(), currentUserId, ActivityType.BOARD_UPDATED,
                    "Board renamed to \"" + name + "\"");
        } catch (Exception ignored) {}

        return saved;
    }

    @Transactional
    public void delete(Long boardId, Authentication auth) {
        if (!boardSecurity.isOwner(boardId, auth)) throw new SecurityException("Only owner can delete board");

        activity.deleteAllForBoard(boardId);
        tasks.deleteAssigneesByBoardId(boardId);
        tasks.deleteByBoardId(boardId);
        lists.deleteByBoardId(boardId);
        members.deleteAllByBoardId(boardId);

        boards.delete(get(boardId));
    }

    @Transactional(readOnly = true)
    public List<BoardMember> membersOf(Long boardId) {
        get(boardId);
        return members.findAllByBoardId(boardId);
    }

    @Transactional
    public BoardMember invite(Long boardId, Long userId, BoardMemberRole role, Authentication auth) {
        if (!boardSecurity.isOwner(boardId, auth)) throw new SecurityException("Not allowed");
        Board b = get(boardId);
        User u = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        BoardMember created = members.findByBoardIdAndUserId(boardId, userId).orElseGet(() ->
                members.save(BoardMember.builder().board(b).user(u).role(role).build()));

        try {
            activity.log(boardId, currentUserId(auth), ActivityType.BOARD_MEMBER_ADDED,
                    "User " + u.getEmail() + " added as " + role);
        } catch (Exception ignored) {}

        return created;
    }

    @Transactional
    public BoardMember updateRole(Long boardId, Long userId, BoardMemberRole role, Authentication auth) {
        if (!boardSecurity.isOwner(boardId, auth)) throw new SecurityException("Not allowed");
        BoardMember m = members.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        m.setRole(role);
        BoardMember saved = members.save(m);

        try {
            User u = saved.getUser();
            activity.log(boardId, currentUserId(auth), ActivityType.BOARD_UPDATED,
                    "User " + u.getEmail() + " role updated to " + role);
        } catch (Exception ignored) {}

        return saved;
    }

    @Transactional
    public void remove(Long boardId, Long userId, Authentication auth) {
        if (!boardSecurity.isOwner(boardId, auth)) throw new SecurityException("Not allowed");
        members.deleteByBoardIdAndUserId(boardId, userId);

        try {
            User u = users.findById(userId).orElse(null);
            String detail = (u != null)
                    ? "User " + u.getEmail() + " removed from board"
                    : "Member removed from board";
            activity.log(boardId, currentUserId(auth), ActivityType.BOARD_MEMBER_REMOVED, detail);
        } catch (Exception ignored) {}
    }
}