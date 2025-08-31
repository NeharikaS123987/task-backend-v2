package com.example.taskmanager.activity;

import com.example.taskmanager.board.Board;
import com.example.taskmanager.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
        name = "activity_logs",
        indexes = {
                @Index(name = "ix_activity_board_created", columnList = "board_id,createdAt"),
                @Index(name = "ix_activity_created", columnList = "createdAt")
        }
)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The board where this activity happened. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @JsonIgnore
    private Board board;

    /** Optional actor (system-generated entries may be null). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    @JsonIgnore
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ActivityType type;

    @Column(length = 500)
    private String detail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}