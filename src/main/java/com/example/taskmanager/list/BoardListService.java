package com.example.taskmanager.list;

import com.example.taskmanager.board.Board;
import com.example.taskmanager.board.BoardRepository;
import com.example.taskmanager.common.NotFoundException;
import com.example.taskmanager.security.BoardSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardListService {

    private final BoardListRepository lists;
    private final BoardRepository boards;
    private final BoardSecurity boardSecurity;

    @Transactional(readOnly = true)
    public List<BoardList> byBoard(Long boardId, Authentication auth) {
        if (!boardSecurity.isMember(boardId, auth)) throw new SecurityException("Not allowed");
        return lists.findByBoardIdOrderByPositionAsc(boardId);
    }

    @Transactional
    public BoardList create(Long boardId, String name, Integer position, Authentication auth) {
        if (!boardSecurity.canEdit(boardId, auth)) throw new SecurityException("Not allowed");
        Board b = boards.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found"));
        BoardList l = BoardList.builder().board(b).name(name).position(position).build();
        return lists.save(l);
    }

    @Transactional
    public BoardList update(Long listId, String name, Integer position, Authentication auth) {
        BoardList l = lists.findById(listId).orElseThrow(() -> new NotFoundException("List not found"));
        if (!boardSecurity.canEdit(l.getBoard().getId(), auth)) throw new SecurityException("Not allowed");
        if (name != null) l.setName(name);
        if (position != null) l.setPosition(position);
        return lists.save(l);
    }

    @Transactional
    public void delete(Long listId, Authentication auth) {
        BoardList l = lists.findById(listId).orElseThrow(() -> new NotFoundException("List not found"));
        if (!boardSecurity.canEdit(l.getBoard().getId(), auth)) throw new SecurityException("Not allowed");
        lists.delete(l);
    }
}