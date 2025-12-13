package com.javalab.taskservice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TaskResponseDto(
        Long idTask,

        String title,

        String description,

        String difficulty,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long idAuthor

) implements Serializable {}