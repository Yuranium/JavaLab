package com.javalab.executionservice.util;

import com.javalab.executionservice.models.enums.ExecutionStatus;
import com.javalab.executionservice.service.ExecutionStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionStatusPublisher
{
    private final ExecutionStateService stateService;

    public void publish(Long taskId, ExecutionStatus status, String message, Object data)
    {
        var session = stateService.getSession(taskId);
        if (session != null)
        {
            stateService.sendStatus(session, status, message, data);
        }
        else
        {
            log.warn("No WebSocket session found for taskId {}", taskId);
        }
    }
}