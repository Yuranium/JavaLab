package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryResponseDto(
        String title,

        String description,

        LocalDateTime createdAt

) implements Serializable {}