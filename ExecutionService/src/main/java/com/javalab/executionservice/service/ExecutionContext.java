package com.javalab.executionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionContext
{
    private final Map<String, WebSocketSession> taskSessions = new ConcurrentHashMap<>();

    public void registerSession(String userId, WebSocketSession session)
    {
        taskSessions.put(userId, session);
    }

    public WebSocketSession getContext(String userId)
    {
        return taskSessions.get(userId);
    }

    public void clearContext(WebSocketSession session)
    {
        log.info("clearing context for session {}", session.getId());
        taskSessions.remove(session.getAttributes().get("auth").toString());
        log.info("session has been cleared");
    }
}