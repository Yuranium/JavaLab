package com.yuranium.userservice.service;

import com.yuranium.userservice.models.dto.userlock.UserLockDuration;
import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
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

    private final KeycloakService keycloakService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${user-lock.key}")
    private String userLockKey;

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

    @Scheduled(fixedRate = 10000)
    public void processLockUsers()
    {
        Set<Object> dueTasks = redisTemplate.opsForZSet()
                .rangeByScore(userLockKey, 0, System.currentTimeMillis());

        if (dueTasks != null && !dueTasks.isEmpty())
            dueTasks.forEach(task -> {
                Instant now = Instant.now();
                UserLockDuration userTask = (UserLockDuration) task;
                redisTemplate.opsForZSet().remove(userLockKey, task);

                if ((userTask.startLock().isAfter(now)))
                    changeActivity(userTask.id());

                if (userTask.endLock().isBefore(now))
                    changeActivity(userTask.id());
            });
    }

    public void dynamicBlock(Long id, UserLockDuration duration)
    {
        if (validLockDate(duration.startLock(), duration.endLock()))
            throw new IllegalArgumentException(
                    "The endLock can't be before the startLock or current time"
            );

        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException(
                    "User with id=%d not found".formatted(id)
            );

        redisTemplate.opsForZSet()
                .add(
                        userLockKey, duration,
                        duration.startLock().toEpochMilli()
                );
    }

    public boolean validLockDate(Instant startLock, Instant endLock)
    {
        return endLock.isBefore(startLock) || endLock.isBefore(Instant.now());
    }
}