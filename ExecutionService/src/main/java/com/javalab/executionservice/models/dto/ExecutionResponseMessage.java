package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.ExecutionStatus;

import java.io.Serializable;
import java.util.Collection;

public record ExecutionResponseMessage(
        ExecutionStatus status,

        String error,

        long executionDuration,

        Collection<TestExecutionResult> testCases

) implements Serializable {}