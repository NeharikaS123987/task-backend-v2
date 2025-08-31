package com.example.taskmanager.board;

/** Role within a board. Used for access control. */
public enum BoardMemberRole {
    OWNER,   // full access
    MEMBER,  // can edit tasks/lists
    VIEWER   // read-only
}