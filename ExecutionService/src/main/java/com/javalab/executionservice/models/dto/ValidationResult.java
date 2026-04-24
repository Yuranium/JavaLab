package com.javalab.executionservice.models.dto;

import java.io.Serializable;
import java.util.Collection;

public record ValidationResult(
        boolean hasErrors,

        Collection<String> errorMessages

) implements Serializable {}