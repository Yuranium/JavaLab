package com.javalab.executionservice.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer
{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Environment environment;

    public void sendExecutionAttemptEvent(Object message)
    {
        kafkaTemplate.send(
                environment.getProperty("spring.kafka.topic-names.successful-attempt-topic"),
                message
        );
    }
}
