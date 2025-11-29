package com.yuranium.userservice.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaSender
{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Environment environment;

    public void sendUserRegisteredEvent(Object message)
    {
        kafkaTemplate.send(
                environment.getProperty("kafka.topic-names.user-registered"),
                message
        );
    }
}