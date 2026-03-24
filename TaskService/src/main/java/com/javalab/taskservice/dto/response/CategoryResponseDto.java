package com.javalab.taskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryResponseDto(
        String title,

        String description,

        Instant createdAt

) implements Serializable {}