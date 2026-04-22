package com.javalab.executionservice.models.dto;

import java.io.Serializable;

public record TestCaseResult(
        int testNumber,

        boolean passed,

        String output,

        String expectedOutput

) implements Serializable {}