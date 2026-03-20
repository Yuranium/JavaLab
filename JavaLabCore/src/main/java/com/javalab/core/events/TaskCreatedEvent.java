package com.javalab.core.events;

import java.io.Serializable;
import java.util.Collection;

public record TaskCreatedEvent(
        String title,

        String difficulty,

        Collection<String> categories

) implements Serializable {}