package com.javalab.core.events;

import java.io.Serializable;

public record TestCasePayload(
        Long id,

        String input,

        String expectedOutput

) implements Serializable {}