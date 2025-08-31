package com.example.taskmanager.board;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    @Query("""
           select bm
           from BoardMember bm
           where bm.board.id = :boardId and bm.user.id = :userId
           """)
    Optional<BoardMember> findByBoardIdAndUserId(@Param("boardId") Long boardId,
                                                 @Param("userId") Long userId);

    @Query("""
           select bm
           from BoardMember bm
           where bm.board.id = :boardId
           """)
    List<BoardMember> findAllByBoardId(@Param("boardId") Long boardId);

    @Query("""
           select (count(bm) > 0)
           from BoardMember bm
           where bm.board.id = :boardId and bm.user.id = :userId
           """)
    boolean existsByBoardIdAndUserId(@Param("boardId") Long boardId,
                                     @Param("userId") Long userId);

    @Modifying
    @Query("""
           delete from BoardMember bm
           where bm.board.id = :boardId and bm.user.id = :userId
           """)
    void deleteByBoardIdAndUserId(@Param("boardId") Long boardId,
                                  @Param("userId") Long userId);

    /** Bulk delete all memberships for a board (used by BoardService.delete). */
    @Modifying
    @Query("delete from BoardMember bm where bm.board.id = :boardId")
    void deleteAllByBoardId(@Param("boardId") Long boardId);
}