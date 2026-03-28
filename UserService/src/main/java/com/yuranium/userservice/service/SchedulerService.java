package com.yuranium.userservice.service;

import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService
{
    private final UserRepository userRepository;

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
}