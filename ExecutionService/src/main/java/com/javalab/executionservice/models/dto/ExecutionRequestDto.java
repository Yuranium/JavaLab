package com.javalab.executionservice.models.dto;

import java.io.Serializable;
import java.util.UUID;

public record ExecutionRequestDto(
        UUID userId,

        Long taskId,

        String code

) implements Serializable {}