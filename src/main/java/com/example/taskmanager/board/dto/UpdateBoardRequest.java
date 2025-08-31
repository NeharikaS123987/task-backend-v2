package com.example.taskmanager.board.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBoardRequest(@NotBlank String name) { }