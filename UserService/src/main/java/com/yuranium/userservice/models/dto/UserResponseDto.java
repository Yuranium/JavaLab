package com.yuranium.userservice.models.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,

        String username,

        String name,

        String lastName,

        String email,

        String avatar,

        LocalDateTime dateRegistration,

        LocalDateTime lastLogin,

        Boolean activity,

        Boolean notifyEnabled,

        String timezone

) implements Serializable {}