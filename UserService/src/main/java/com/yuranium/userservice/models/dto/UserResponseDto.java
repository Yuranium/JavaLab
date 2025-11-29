package com.yuranium.userservice.models.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,

        String username,

        String name,

        String lastName,

        LocalDateTime dateRegistration,

        LocalDateTime lastLogin,

        Boolean activity

) implements Serializable {}