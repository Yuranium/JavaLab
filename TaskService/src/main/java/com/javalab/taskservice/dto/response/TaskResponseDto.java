package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskResponseDto(
        Long idTask,

        String title,

        String description,

        String difficulty,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long idAuthor,

        Collection<CategoryResponseDto> categories,

        StarterCodeResponseDto starterCode

) implements Serializable {}