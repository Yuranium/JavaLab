package com.javalab.executionservice.models.dto;

import java.io.Serializable;

public record ExecutionRequestDto(
        Long taskId,

        String code

) implements Serializable {}