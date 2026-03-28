package com.yuranium.userservice.models.dto;

import java.io.Serializable;
import java.time.Instant;

public record UserResponseDto(
        String username,

        String name,

        String lastName,

        String email,

        String avatar,

        Instant dateRegistration,

        Instant lastLogin,

        Boolean activity,

        Boolean notifyEnabled,

        String timezone

) implements Serializable {}