package com.javalab.executionservice.service;

import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.dto.ExecutionResponseMessage;
import com.javalab.executionservice.models.enums.ExecutionStatus;
import com.javalab.executionservice.util.ExecutionValidator;
import com.javalab.executionservice.util.ws.ExecutionStatusPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionWebSocketHandler extends TextWebSocketHandler
{
    private final ExecutionService executionService;

    private final ExecutionContext executionContext;

    private final ExecutionValidator validator;

    private final ObjectMapper objectMapper;

    private final ExecutionStatusPublisher publisher;

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
    {
        log.info("WS connection closed");
        executionContext.clearContext(session);
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session, TextMessage message
    ) throws Exception
    {
        try
        {
            log.info("WS received '{}' in session {}", message.getPayload(), session.getId());
            var request = objectMapper.readValue(message.getPayload(), ExecutionRequestDto.class);
            var validateResult = validator.validate(request.code());
            String userId = Objects.requireNonNull(session.getPrincipal()).getName();
            executionContext.registerSession(
                    userId,
                    session
            );

            if (validateResult.hasErrors())
            {
                publisher.sendExecutionResult(userId, new ExecutionResponseMessage(
                        ExecutionStatus.FAILED,
                        totalErrors(validateResult.errorMessages()),
                        0L, Collections.emptyList()
                ));
                executionContext.clearContext(session);
                return;
            }
            executionService.execute(request, userId);
        } catch (Exception ex)
        {
            log.warn(ex.getMessage(), ex);
            session.sendMessage(new TextMessage(ex.getMessage()));
        }
        super.handleTextMessage(session, message);
    }

    private String totalErrors(Collection<String> errors)
    {
        return String.join(",", errors);
    }
}