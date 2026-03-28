package com.yuranium.userservice.models.dto;

import java.io.Serializable;
import java.time.Instant;

public record RedisFailedTask(
        String task,

        String message,

        Instant timestamp

) implements Serializable {}