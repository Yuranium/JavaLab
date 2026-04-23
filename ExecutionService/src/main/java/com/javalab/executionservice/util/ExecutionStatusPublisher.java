package com.javalab.executionservice.util;

import com.javalab.executionservice.models.enums.ExecutionStatus;
import com.javalab.executionservice.service.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionStatusPublisher
{
    private final ExecutionContext executionContext;

    public void publishMessage(UUID userId, ExecutionStatus status, String message)
    {
        var session = executionContext.getContext(userId);
        if (session.isOpen())
        {
            try
            {
                session.sendMessage(new TextMessage(status.name() + message));
            } catch (IOException e)
            {
                log.error(e.getMessage());
            }
        }
    }
}