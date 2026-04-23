package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskResponseDto(
        Long idTask,

        String title,

        String difficulty,

        Instant createdAt,

        Instant updatedAt,

        UUID idAuthor,

        Collection<CategoryResponseDto> categories

) implements Serializable {}