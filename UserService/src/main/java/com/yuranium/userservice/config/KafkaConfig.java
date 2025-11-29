package com.yuranium.userservice.config;

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
    public NewTopic userRegisteredTopic()
    {
        return TopicBuilder.name(environment.getProperty("kafka.topic-names.user-registered"))
                .partitions(1)
                .replicas(1)
                .build();
    }
}