package com.yuranium.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuranium.userservice.enums.LockAction;
import com.yuranium.userservice.models.dto.userlock.UserLockDuration;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, String> redisTemplate;

    private final UserLockService userLockService;

    @Value("${spring.redis.user-lock.key}")
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

    @SneakyThrows
    @Scheduled(fixedDelay = 10000)
    public void changeActivityState()
    {
        long now = System.currentTimeMillis();
        Set<String> dueTasks = redisTemplate.opsForZSet()
                .rangeByScore(userLockKey, 0, now);

        if (dueTasks != null)
            for (String task : dueTasks)
            {
                UserLockTask currentTask = objectMapper.readValue(task, UserLockTask.class);
                userLockService.executeLockAction(currentTask);
                redisTemplate.opsForZSet().remove(userLockKey, task);
            }
    }

    @SneakyThrows
    public void dynamicLock(Long id, UserLockDuration duration)
    {
        if (!validLockDate(duration.startLock(), duration.endLock()))
            throw new IllegalArgumentException(
                    "The endLock can't be before the startLock or current time"
            );
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(id)
                ));

        UserLockTask lockTask = new UserLockTask(userEntity.getKeycloakId(), LockAction.LOCK);
        String lockJson = objectMapper.writeValueAsString(lockTask);
        redisTemplate.opsForZSet().add(userLockKey, lockJson, duration.startLock().toEpochMilli());
        if (!duration.isPermanentLock())
        {
            UserLockTask unlockTask = new UserLockTask(
                    userEntity.getKeycloakId(),
                    LockAction.UNLOCK
            );
            String unlockJson = objectMapper.writeValueAsString(unlockTask);
            redisTemplate.opsForZSet().add(userLockKey, unlockJson, duration.endLock().toEpochMilli());
        }
    }

    public boolean validLockDate(Instant startLock, Instant endLock)
    {
        return !endLock.isBefore(startLock) || !endLock.isBefore(Instant.now());
    }
}