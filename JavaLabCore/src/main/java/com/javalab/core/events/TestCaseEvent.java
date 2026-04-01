package com.javalab.core.events;

import java.io.Serializable;

public record TestCaseEvent(
        TestCaseEventType type,

        Long taskId,

        TestCasePayload payload

) implements Serializable {}