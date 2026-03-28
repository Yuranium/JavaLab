package com.yuranium.userservice.models.dto;

import java.io.Serializable;
import java.time.Instant;

public record PublicUserResponseDto(
        String username,

        String name,

        String lastName,

        String avatar,

        Instant dateRegistration,

        Instant lastLogin,

        Boolean activity,

        Boolean notifyEnabled

) implements Serializable {}