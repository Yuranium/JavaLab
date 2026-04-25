package com.javalab.executionservice.models.dto;

import java.io.Serializable;

public record TestCaseDto(
        String input,

        String expectedOutput,

        boolean isHidden

) implements Serializable {}