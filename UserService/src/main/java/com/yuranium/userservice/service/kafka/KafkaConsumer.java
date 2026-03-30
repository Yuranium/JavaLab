package com.yuranium.userservice.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalab.core.events.ExternalAuthEvent;
import com.javalab.core.events.UserLoggedInEvent;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer
{
    private final UserService userService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic-names.user-logged-in}")
    public void handle(@Payload String message)
    {
        try
        {
            UserLoggedInEvent event = objectMapper
                    .readValue(message, UserLoggedInEvent.class);

            userService.updateLastLogin(
                    event.keycloakUserId(),
                    Instant.ofEpochMilli(event.loginTimestamp())
            );
        } catch (Exception e)
        {
            log.error("Error while handling UserLoggedInEvent", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic-names.oauth2-user-logged-in}")
    public void handle1(@Payload String message)
    {
        try
        {
            ExternalAuthEvent event = objectMapper
                    .readValue(message,ExternalAuthEvent.class);

            userService.updateOAuth2User(event);
        } catch (Exception e)
        {
            log.error("Error while handling ExternalAuthEvent", e);
        }
    }
}