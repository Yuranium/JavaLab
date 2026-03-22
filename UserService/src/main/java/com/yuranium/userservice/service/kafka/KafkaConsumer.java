package com.yuranium.userservice.service.kafka;

import com.javalab.core.events.UserLoggedInEvent;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${kafka.topic-names.user-logged-in}")
public class KafkaConsumer
{
    private final UserService userService;

    @KafkaHandler
    public void handle(@Payload UserLoggedInEvent event)
    {
        try
        {
            LocalDateTime loginTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.loginTimestamp()),
                    ZoneId.systemDefault()
            );
            userService.updateLastLogin(event.keycloakUserId(), loginTime);
        } catch (Exception e)
        {
            log.error("Error while handling UserLoggedInEvent", e);
        }
    }
}