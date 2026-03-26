package com.yuranium.userservice.models.dto.userlock;

import com.yuranium.userservice.enums.LockAction;

import java.io.Serializable;

public record UserLockTask(
        Long userId,

        LockAction action

) implements Serializable {}