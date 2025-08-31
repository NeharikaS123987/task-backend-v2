package com.example.taskmanager.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByListId(Long listId);

    @Query("""
        select t from Task t
        join t.list l
        join l.board b
        where b.id = :boardId
    """)
    Page<Task> findByBoard(@Param("boardId") Long boardId, Pageable pageable);

    /** Remove assignee rows for all tasks under a board (clears the join table first). */
    @Modifying
    @Query(value = """
        delete from task_assignees ta
        where ta.task_id in (
            select t.id
            from task t
            join board_lists bl on t.list_id = bl.id
            where bl.board_id = :boardId
        )
        """, nativeQuery = true)
    void deleteAssigneesByBoardId(@Param("boardId") Long boardId);

    /** Bulk delete all tasks under a board. */
    @Modifying
    @Query("delete from Task t where t.list.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}