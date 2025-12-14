package com.javalab.taskservice.dto.request;

import java.io.Serializable;

public record StarterCodeRequestDto(
        String code,

        Boolean isDefault

) implements Serializable {}