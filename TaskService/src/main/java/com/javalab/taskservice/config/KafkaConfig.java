package com.javalab.taskservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig
{
    private final Environment environment;

    @Bean
    public NewTopic taskCreatedTopic()
    {
        return TopicBuilder.name(environment.getProperty("kafka.topic-names.task-created"))
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic testCaseEventsTopic()
    {
        return TopicBuilder.name(environment.getProperty("kafka.topic-names.test-case-events"))
                .partitions(1)
                .replicas(1)
                .build();
    }
}