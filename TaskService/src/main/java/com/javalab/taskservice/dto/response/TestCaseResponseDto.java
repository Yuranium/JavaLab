package com.javalab.taskservice.dto.response;

import java.io.Serializable;

public record TestCaseResponseDto(
        Long id,

        String input,

        String expectedOutput

) implements Serializable {}