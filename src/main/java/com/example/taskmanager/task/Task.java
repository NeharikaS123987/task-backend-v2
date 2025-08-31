package com.example.taskmanager.task;

import com.example.taskmanager.list.BoardList;
import com.example.taskmanager.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    @JsonIgnore
    private BoardList list;

    /**
     * We ignore assignees in Stage 1 responses to avoid LazyInitializationException during
     * serialization (controller returns entity outside of the transaction). Stage 4 can
     * switch to DTOs if we want to expose this field.
     */
    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @JsonIgnore
    private Set<User> assignees = new HashSet<>();

    /** Audit fields */
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    private Instant completedAt;
}