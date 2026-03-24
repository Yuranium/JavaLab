package com.yuranium.userservice.models.dto.userlock;

import com.yuranium.userservice.enums.LockAction;

import java.io.Serializable;
import java.util.UUID;

public record UserLockTask(
        UUID userId,

        LockAction action

) implements Serializable {}