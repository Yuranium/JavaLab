package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.ExecutionStatus;

import java.io.Serializable;

public record ExecutionResponseMessage(
        ExecutionStatus status,

        String message,

        Object data

) implements Serializable {}