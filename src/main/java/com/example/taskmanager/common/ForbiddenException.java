package com.example.taskmanager.common;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}