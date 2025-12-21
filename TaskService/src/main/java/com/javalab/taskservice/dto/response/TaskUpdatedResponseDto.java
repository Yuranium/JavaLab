package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskUpdatedResponseDto(
        Long id,

        String title,

        String description,

        String difficulty,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long idAuthor

) implements Serializable {}