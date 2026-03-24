package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskDetailedResponseDto(
        Long idTask,

        String title,

        String description,

        String difficulty,

        Instant createdAt,

        Instant updatedAt,

        Long idAuthor,

        Collection<CategoryResponseDto> categories,

        StarterCodeResponseDto starterCode,

        Collection<TestCaseResponseDto> testCases

) implements Serializable
{
    public TaskDetailedResponseDto(
            TaskResponseDto dto,
            Collection<TestCaseResponseDto> testCases
    )
    {
        this(dto.idTask(), dto.title(), dto.description(), dto.difficulty(),
                dto.createdAt(), dto.updatedAt(), dto.idAuthor(), dto.categories(),
                dto.starterCode(), testCases);
    }
}