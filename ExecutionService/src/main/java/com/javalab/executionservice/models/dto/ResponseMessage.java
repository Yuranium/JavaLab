package com.javalab.executionservice.models.dto;

import com.javalab.executionservice.models.enums.MessageType;

import java.io.Serializable;
import java.time.Instant;

public record ResponseMessage(
        MessageType type,

        Object payload,

        Instant timestamp

) implements Serializable {}