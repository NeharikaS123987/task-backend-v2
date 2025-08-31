package com.example.taskmanager.board;

/** Role within a board. */
public enum BoardRole {
    OWNER,   // full access
    EDITOR,  // modify tasks/lists but not board settings
    VIEWER   // read-only
}