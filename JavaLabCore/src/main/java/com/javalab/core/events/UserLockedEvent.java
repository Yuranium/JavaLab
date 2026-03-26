package com.javalab.core.events;

import java.io.Serializable;
import java.time.OffsetDateTime;

public record UserLockedEvent(
        String username,

        String email,

        OffsetDateTime startLock,

        OffsetDateTime endLock,

        boolean isLock,

        String message

) implements Serializable {}