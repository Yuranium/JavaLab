package com.javalab.taskservice.dto.response;

import java.io.Serializable;
import java.util.Collection;

public record TaskCreatedResponseDto(
        TaskResponseDto task,

        Collection<CategoryResponseDto> categories,

        StarterCodeResponseDto starterCode

) implements Serializable {}