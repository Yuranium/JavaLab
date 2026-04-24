package com.javalab.core.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record ExecutionAttemptEvent(
        boolean isCorrect,

        String code,

        UUID userId,

        Long taskId,

        Instant createdAt

) implements Serializable {}