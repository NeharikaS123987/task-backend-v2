package com.example.taskmanager.user;

import com.example.taskmanager.board.BoardMember;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "users",
        uniqueConstraints = {
                // keep email unique at the table level
                @UniqueConstraint(columnNames = "email")
                // username is optional; we'll keep uniqueness via the column annotation below
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login + unique identity used as JWT subject. */
    @Column(nullable = false, unique = true, length = 190) // 190 safe for most MySQL index defaults
    private String email;

    /**
     * Optional handle. Do NOT require it at signup.
     * Still unique when present, but null is allowed.
     */
    @Column(unique = true) // nullable by default; allows signup without username
    private String username;

    /** BCrypt hash (never expose). */
    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    /** Optional friendly display name. */
    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<BoardMember> memberships = new ArrayList<>();
}