package com.javalab.taskservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TestCaseRequestDto(
    String expectedOutput,

    Boolean isHidden

) implements Serializable {}