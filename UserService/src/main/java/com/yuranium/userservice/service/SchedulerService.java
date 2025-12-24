package com.yuranium.userservice.service;

import com.yuranium.userservice.repository.UserIdempotencyRepository;
import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SchedulerService
{
    private final UserRepository userRepository;

    private final UserIdempotencyRepository idempotencyRepository;

    @Value("${spring.application.scheduler.delete-id-key.key-lifetime}")
    private Duration idempotencyKeyLifetime;

    @Scheduled(
            cron = "${spring.application.scheduler.delete-user.cron}",
            zone = "${spring.application.scheduler.zone}"
    )
    public void deleteInactiveUsers()
    {
        userRepository.deleteInactiveUsers();
    }

    @Scheduled(
            cron = "${spring.application.scheduler.delete-id-key.cron}",
            zone = "${spring.application.scheduler.zone}"
    )
    public void deleteIdempotencyKey()
    {
        idempotencyRepository.deleteExpiredIdempotencyKey(idempotencyKeyLifetime);
    }
}