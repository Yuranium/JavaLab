package com.yuranium.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuranium.userservice.enums.LockAction;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserLockActionService
{
    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    @Value("${spring.redis.user-lock.key}")
    private String queueKey;

    @SneakyThrows
    public void scheduleTask(Long userId, LockAction action, Instant executeAt)
    {
        UserLockTask task = new UserLockTask(userId, action);
        String taskJson = objectMapper.writeValueAsString(task);
        redisTemplate.opsForZSet().add(queueKey, taskJson, executeAt.toEpochMilli());
    }
}