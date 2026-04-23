package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.ExecutionStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;

public record ExecutionResponseMessage(
        ExecutionStatus status,

        String error,

        long executionTDuration,

        Instant timestamp,

        Collection<TestCaseResult> testCases

) implements Serializable {}