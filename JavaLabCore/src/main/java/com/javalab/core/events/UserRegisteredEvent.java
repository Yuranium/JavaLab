package com.javalab.core.events;

import java.io.Serializable;
import java.util.UUID;

public record UserRegisteredEvent(
        Long id,

        UUID keycloakId,

        String username,

        String email,

        Integer authCode

) implements Serializable {}