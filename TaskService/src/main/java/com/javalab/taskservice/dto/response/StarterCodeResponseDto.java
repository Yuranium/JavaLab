package com.javalab.taskservice.dto.response;

import java.io.Serializable;

public record StarterCodeResponseDto(
        Long id,

        String code,

        Boolean isDefault

) implements Serializable {}