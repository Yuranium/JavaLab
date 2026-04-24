package com.javalab.executionservice.util.ws;

import com.javalab.executionservice.models.dto.ExecutionResponseMessage;
import com.javalab.executionservice.models.dto.InfoMessage;
import com.javalab.executionservice.models.dto.ResponseMessage;
import com.javalab.executionservice.models.dto.TestExecutionResult;
import com.javalab.executionservice.models.enums.MessageType;
import com.javalab.executionservice.service.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionStatusPublisher
{
    private final ExecutionContext executionContext;

    private final ObjectMapper objectMapper;

    public void sendInfo(String userId, String message)
    {
        var session = executionContext.getContext(userId);
        if (session.isOpen())
        {
            try
            {
                send(session, new ResponseMessage(
                        MessageType.INFO,
                        new InfoMessage(message),
                        Instant.now()
                ));
            } catch (IOException e)
            {
                log.error(e.getMessage());
            }
        }
    }

    public void sendTestResult(String userId, TestExecutionResult result)
    {
        var session = executionContext.getContext(userId);
        if (session.isOpen())
        {
            try
            {
                send(session, new ResponseMessage(
                        MessageType.TEST_RESULT,
                        result, Instant.now()
                ));
            } catch (IOException e)
            {
                log.error(e.getMessage());
            }
        }
    }

    public void sendExecutionResult(String userId, ExecutionResponseMessage result)
    {
        var session = executionContext.getContext(userId);
        if (session.isOpen())
        {
            try
            {
                send(session, new ResponseMessage(
                        MessageType.FINAL_RESULT,
                        result, Instant.now()
                ));
            } catch (IOException e)
            {
                log.error(e.getMessage());
            }
        }
    }

    private void send(WebSocketSession session, ResponseMessage responseMessage) throws IOException
    {
        session.sendMessage(
                new TextMessage(
                        objectMapper.writeValueAsBytes(responseMessage)
                ));
    }
}