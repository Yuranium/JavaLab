package com.javalab.core.events;

import java.io.Serializable;
import java.util.UUID;

public record ExternalAuthEvent(
        UUID eventId,

        UUID keycloakId,

        String username,

        String fullName,

        String firstName,

        String lastName,

        String email,

        String avatarUrl,

        Long timestamp

) implements Serializable {}