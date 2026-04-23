package com.javalab.executionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionContext
{
    private final Map<String, WebSocketSession> taskSessions = new ConcurrentHashMap<>();

    public void registerTask(UUID userId, WebSocketSession session)
    {
        taskSessions.put(userId.toString(), session);
    }

    public WebSocketSession getContext(UUID userId)
    {
        return taskSessions.get(userId.toString());
    }
}