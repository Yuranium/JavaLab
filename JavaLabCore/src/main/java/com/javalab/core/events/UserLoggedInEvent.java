package com.javalab.core.events;

import java.io.Serializable;
import java.util.UUID;

public record UserLoggedInEvent(
        UUID eventId,

        UUID keycloakUserId,

        String realmId,

        long loginTimestamp

) implements Serializable {}