package com.javalab.taskservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRequestDto(
        String title,

        String description,

        String difficulty,

        Long idAuthor,

        StarterCodeRequestDto starterCode,

        Collection<String> categories,

        Collection<TestCaseRequestDto> testCases

) implements Serializable {}