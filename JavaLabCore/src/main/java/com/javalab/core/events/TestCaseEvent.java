package com.javalab.core.events;

import java.io.Serializable;
import java.util.Collection;

public record TestCaseEvent(
        TestCaseEventType type,

        Long taskId,

        Collection<TestCasePayload> payload

) implements Serializable {}