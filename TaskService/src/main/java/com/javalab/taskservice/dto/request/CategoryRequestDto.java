package com.javalab.taskservice.dto.request;

import java.io.Serializable;

public record CategoryRequestDto(
        String title,

        String description

) implements Serializable {}