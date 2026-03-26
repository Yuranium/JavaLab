package com.yuranium.userservice.models.dto.userlock;

import java.io.Serializable;
import java.time.Instant;

public record UserUnlockRequest(
        Instant unlockTime

) implements Serializable {}