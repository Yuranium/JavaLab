package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.TestCaseStatus;

import java.io.Serializable;

public record TestCaseResult(
        int testNumber,

        boolean passed,

        TestCaseStatus testCaseStatus,

        String output,

        String expectedOutput,

        String error,

        long executionDuration

) implements Serializable {}