package com.yuranium.userservice.service;

import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService
{
    private final UserRepository userRepository;

    @Scheduled(
            cron = "${spring.application.scheduler.delete-user.cron}",
            zone = "${spring.application.scheduler.delete-user.zone}"
    )
    public void deleteInactiveUsers()
    {
        userRepository.deleteInactiveUsers();
    }
}