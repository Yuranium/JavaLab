package com.javalab.taskservice.dto.response;

import java.io.Serializable;

public record TestCaseResponseDto(
        String input,

        String expectedOutput,

        Boolean isHidden,

        Long taskId

) implements Serializable {}