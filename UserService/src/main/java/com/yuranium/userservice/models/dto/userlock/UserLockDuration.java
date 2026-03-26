package com.yuranium.userservice.models.dto.userlock;

import java.io.Serializable;
import java.time.Instant;

public record UserLockDuration(
        Instant startLock,

        Instant endLock,

        String message

) implements Serializable {}