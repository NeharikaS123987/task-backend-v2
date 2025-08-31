package com.example.taskmanager.board;

import com.example.taskmanager.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"board_id","member_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BoardMembership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JsonIgnore
    private Board board;

    @ManyToOne(optional=false) @JoinColumn(name="member_id")
    private User member;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private BoardRole role;
}