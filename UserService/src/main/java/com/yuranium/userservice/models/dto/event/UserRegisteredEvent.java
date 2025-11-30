package com.yuranium.userservice.models.dto.event;

import java.io.Serializable;

public record UserRegisteredEvent(
        Long id,

        String username,

        String email,

        Integer authCode

) implements Serializable {}