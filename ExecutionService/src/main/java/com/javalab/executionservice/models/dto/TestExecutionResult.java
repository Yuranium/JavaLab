package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.TestCaseStatus;

import java.io.Serializable;

public record TestExecutionResult(
        int testNumber,

        TestCaseStatus status,

        String output,

        String exceptedOutput,

        String error,

        long executionDuration

) implements Serializable
{
    public boolean isPassed()
    {
        return status == TestCaseStatus.PASSED;
    }
}