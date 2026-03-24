package com.javalab.taskservice.dto.response;

import com.javalab.taskservice.enums.DifficultyType;
import com.javalab.taskservice.enums.JavaCategory;

import java.io.Serializable;
import java.util.Collection;

public record TaskAttributeResponseDto(
        Collection<DifficultyType> difficulties,

        Collection<JavaCategory> categories

) implements Serializable {}