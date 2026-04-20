package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.ExecutionStatus;

import java.io.Serializable;

public record ExecutionStatusDto(
        ExecutionStatus status

) implements Serializable {}