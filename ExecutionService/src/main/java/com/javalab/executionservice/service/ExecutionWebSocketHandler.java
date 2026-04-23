package com.javalab.executionservice.service;

import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.util.ExecutionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionWebSocketHandler extends TextWebSocketHandler
{
    private final ExecutionService executionService;

    private final ExecutionContext stateService;

    private final ExecutionValidator validator;

    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
    {
        log.info("WS connection closed");
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session, TextMessage message
    ) throws Exception
    {
        log.info("WS received '{}' in session {}", message.getPayload(), session.getId());
        var request = objectMapper.readValue(message.getPayload(), ExecutionRequestDto.class);
        var validateResult = validator.validate(request.code());
        if (validateResult.hasErrors())
        {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(validateResult)));
            return;
        }
        stateService.registerTask(request.userId(), session);
        executionService.execute(request);
        super.handleTextMessage(session, message);
    }
}
