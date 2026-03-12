package com.yuranium.javalabcore.events;

import java.io.Serializable;

public record UserRegisteredEvent(
        Long id,

        String username,

        String email,

        Integer authCode

) implements Serializable {}