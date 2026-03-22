package com.yuranium.userservice.service;

import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

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
        LocalDateTime offsetDateTime = LocalDateTime.now().minus(inactiveUserKeyLifetime);
        userRepository.deleteInactiveUsers(offsetDateTime);
    }
}