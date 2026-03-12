package com.javalab.taskservice.service.kafka;

import com.yuranium.javalabcore.events.TaskCreatedEvent;
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

    public void sendTaskCreatedEvent(TaskCreatedEvent event)
    {
        kafkaTemplate.send(
                environment.getProperty("kafka.topic-names.task-created"),
                event
        );
    }
}