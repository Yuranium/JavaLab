package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskResponseDto(
        Long idTask,

        String title,

        String description,

        String difficulty,

        Instant createdAt,

        Instant updatedAt,

        Long idAuthor,

        Collection<CategoryResponseDto> categories

) implements Serializable {}