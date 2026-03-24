package com.yuranium.userservice.models.dto.userlock;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record UserLockDuration(
        UUID id,

        Instant startLock,

        Instant endLock,

        Boolean isPermanentLock

) implements Serializable {}