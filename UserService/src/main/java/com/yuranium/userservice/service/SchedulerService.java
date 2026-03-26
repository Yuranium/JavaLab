package com.yuranium.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService
{
    private final UserRepository userRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    private final UserLockService lockService;

    @Value("${spring.redis.user-lock.key}")
    private String queueKey;

    @Value("${spring.application.scheduler.delete-user.key-lifetime}")
    private Duration inactiveUserKeyLifetime;

    @Scheduled(
            cron = "${spring.application.scheduler.delete-user.cron}",
            zone = "${spring.application.scheduler.zone}"
    )
    public void deleteInactiveUsers()
    {
        Instant offsetDateTime = Instant.now().minus(inactiveUserKeyLifetime);
        userRepository.deleteInactiveUsers(offsetDateTime);
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 10000)
    public void processDueTasks()
    {
        long now = System.currentTimeMillis();
        Set<String> dueTasks = redisTemplate.opsForZSet().rangeByScore(queueKey, 0, now);
        if (dueTasks == null || dueTasks.isEmpty())
            return;

        for (String taskJson : dueTasks)
        {
            UserLockTask task = objectMapper.readValue(taskJson, UserLockTask.class);
            lockService.executeLockAction(task);
            redisTemplate.opsForZSet().remove(queueKey, taskJson);
        }
    }
}