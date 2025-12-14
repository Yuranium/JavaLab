package com.javalab.taskservice.dto.response;

import java.io.Serializable;

public record StarterCodeResponseDto(
        String code,

        Boolean isDefault

) implements Serializable {}