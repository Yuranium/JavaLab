package com.javalab.core.events;

import java.io.Serializable;

public record TestCasePayload(
        String input,

        String expectedOutput

) implements Serializable {}