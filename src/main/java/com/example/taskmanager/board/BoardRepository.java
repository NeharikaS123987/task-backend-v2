package com.example.taskmanager.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("""
        select distinct b from BoardMember m
        join m.board b
        where m.user.id = :userId
    """)
    List<Board> findAllByMemberUserId(@Param("userId") Long userId);

    /** Load a board with its lists only; tasks initialized in service to avoid multiple-bag fetch. */
    @Query("""
        select distinct b
        from Board b
        left join fetch b.lists l
        where b.id = :id
    """)
    Optional<Board> findByIdWithLists(@Param("id") Long id);
}