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
            userService.updateLastLogin(
                    event.keycloakUserId(),
                    Instant.ofEpochMilli(event.loginTimestamp())
            );
        } catch (Exception e)
        {
            log.error("Error while handling UserLoggedInEvent", e);
        }
    }
}