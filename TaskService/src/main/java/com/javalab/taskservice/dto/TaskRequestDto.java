package com.javalab.taskservice.dto;

import java.io.Serializable;

public record TaskRequestDto(
        String title,

        String description,

        String difficulty,

        Long idAuthor

) implements Serializable {}