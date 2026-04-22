package com.javalab.executionservice.service;

import com.javalab.executionservice.models.dto.ExecutionResponseMessage;
import com.javalab.executionservice.models.enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionStateService
{
    private final ObjectMapper objectMapper;

    private final Map<Long, WebSocketSession> taskSessions = new ConcurrentHashMap<>();

    public void registerTask(Long taskId, WebSocketSession session)
    {
        taskSessions.put(taskId, session);
    }

    public WebSocketSession getSession(Long taskId)
    {
        return taskSessions.get(taskId);
    }

    public void sendStatus(
            WebSocketSession session,
            ExecutionStatus status,
            String message,
            Object data
    )
    {
        try
        {
            if (session.isOpen())
            {
                var wsMsg = new ExecutionResponseMessage(status, message, data);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(wsMsg)));
            }
        } catch (Exception e)
        {
            log.warn("Failed to send message. ", e);
        }
    }
}