package com.javalab.taskservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.javalab.taskservice.enums.DifficultyType;
import com.javalab.taskservice.enums.JavaCategory;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRequestDto(
        String title,

        String description,

        DifficultyType difficulty,

        UUID idAuthor,

        StarterCodeRequestDto starterCode,

        Collection<JavaCategory> categories,

        Collection<TestCaseRequestDto> testCases

) implements Serializable {}