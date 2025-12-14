package com.javalab.taskservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskResponseDto(
        Long idTask,

        String title,

        String description,

        String difficulty,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long idAuthor

) implements Serializable {}