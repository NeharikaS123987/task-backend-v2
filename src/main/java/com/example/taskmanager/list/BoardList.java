package com.example.taskmanager.list;

import com.example.taskmanager.board.Board;
import com.example.taskmanager.task.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "board_lists")
public class BoardList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    /** Position/order in the board (for Kanban ordering). */
    private Integer position;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnore
    private Board board;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore // <- important: avoid LazyInitializationException when returning BoardList directly
    private List<Task> tasks = new ArrayList<>();
}