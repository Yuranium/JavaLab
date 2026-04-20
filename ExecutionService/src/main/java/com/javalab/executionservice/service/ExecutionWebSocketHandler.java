package com.javalab.executionservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ExecutionWebSocketHandler extends TextWebSocketHandler
{
    private final ExecutionService executionService;

    private final ExecutionStateService stateService;

    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
    {
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session, TextMessage message
    ) throws Exception
    {
        super.handleTextMessage(session, message);
    }
}
