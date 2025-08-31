package com.example.taskmanager.list;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardListRepository extends JpaRepository<BoardList, Long> {
    List<BoardList> findByBoardIdOrderByPositionAsc(Long boardId);

    /** Bulk delete lists for a board. */
    void deleteByBoardId(Long boardId);
}