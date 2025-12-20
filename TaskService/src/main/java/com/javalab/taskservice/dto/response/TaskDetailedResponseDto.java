package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskDetailedResponseDto(
        Long idTask,

        String title,

        String description,

        String difficulty,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long idAuthor,

        Collection<CategoryResponseDto> categories,

        StarterCodeResponseDto starterCode,

        Collection<TestCaseResponseDto> testCases

) implements Serializable
{
    public TaskDetailedResponseDto(TaskResponseDto dto, Collection<TestCaseResponseDto> testCases)
    {
        this(dto.idTask(), dto.title(), dto.description(), dto.difficulty(),
                dto.createdAt(), dto.updatedAt(), dto.idAuthor(), dto.categories(),
                dto.starterCode(), testCases);
    }

    public TaskDetailedResponseDto(
            Long idTask, String title, String description, String difficulty,
            LocalDateTime createdAt, LocalDateTime updatedAt, Long idAuthor,
            Collection<CategoryResponseDto> categories, StarterCodeResponseDto starterCode,
            Collection<TestCaseResponseDto> testCases
    )
    {
        this.idTask = idTask;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.idAuthor = idAuthor;
        this.categories = categories;
        this.starterCode = starterCode;
        this.testCases = testCases;
    }
}