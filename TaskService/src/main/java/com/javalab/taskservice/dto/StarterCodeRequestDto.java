package com.javalab.taskservice.dto;

import java.io.Serializable;

public record StarterCodeRequestDto(
        String code,

        Boolean isDefault

) implements Serializable {}