package com.javalab.core.events;

import java.io.Serializable;

public record UserRegisteredEvent(
        Long id,

        String username,

        String email,

        Integer authCode

) implements Serializable {}