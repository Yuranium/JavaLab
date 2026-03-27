package com.yuranium.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.UUID;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.redis.user-lock")
public class RedisConfig
{
    private String queueKey;

    private String processedPrefix;

    private String dlqKey;

    private Duration processedTtl;

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory)
    {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    public String getProcessedKey(UUID taskId)
    {
        return processedPrefix + taskId;
    }
}