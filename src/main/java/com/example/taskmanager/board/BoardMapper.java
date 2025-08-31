package com.example.taskmanager.board;

import com.example.taskmanager.board.dto.BoardDto;
import com.example.taskmanager.board.dto.BoardWithListsDto;
import com.example.taskmanager.board.dto.ListDto;
import com.example.taskmanager.task.Task;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BoardMapper {
    private BoardMapper() {}

    /** Map a Board entity to a compact BoardDto (lists without tasks). */
    public static BoardDto toBoardDto(Board board) {
        List<ListDto> lists;

        if (board.getLists() != null && Hibernate.isInitialized(board.getLists())) {
            lists = board.getLists().stream()
                    .filter(Objects::nonNull)
                    .map(l -> new ListDto(l.getId(), l.getName(), l.getPosition()))
                    .toList();
        } else {
            // Transaction likely ended; don't touch lazy collections.
            lists = List.of();
        }

        return new BoardDto(board.getId(), board.getName(), lists);
    }

    /** Map a Board entity to a BoardWithListsDto, including list task summaries. */
    public static BoardWithListsDto toBoardWithListsDto(Board board) {
        Long ownerId = board.getOwner() != null ? board.getOwner().getId() : null;

        List<BoardWithListsDto.ListSummary> listSummaries;
        if (board.getLists() != null && Hibernate.isInitialized(board.getLists())) {
            listSummaries = board.getLists().stream()
                    .filter(Objects::nonNull)
                    .map(l -> new BoardWithListsDto.ListSummary(
                            l.getId(),
                            l.getName(),
                            l.getPosition(),
                            // Only touch tasks if initialized
                            (l.getTasks() != null && Hibernate.isInitialized(l.getTasks()))
                                    ? l.getTasks().stream()
                                    .map(BoardMapper::toTaskSummary)
                                    .collect(Collectors.toList())
                                    : List.of()
                    ))
                    .collect(Collectors.toList());
        } else {
            listSummaries = List.of();
        }

        return new BoardWithListsDto(board.getId(), board.getName(), ownerId, listSummaries);
    }

    /** Convert Task to lightweight summary used inside BoardWithListsDto. */
    public static BoardWithListsDto.TaskSummary toTaskSummary(Task t) {
        return new BoardWithListsDto.TaskSummary(
                t.getId(),
                t.getTitle(),
                t.getStatus()
        );
    }
}