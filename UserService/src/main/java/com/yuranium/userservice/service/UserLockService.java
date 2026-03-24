package com.yuranium.userservice.service;

import com.yuranium.userservice.enums.LockAction;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLockService
{
    private final UserRepository userRepository;

    private final KeycloakService keycloakService;

    @Transactional
    @Retryable(
            retryFor = ConnectException.class,
            backoff = @Backoff(delay = 5000, multiplier = 2, maxDelay = 10000)
    )
    public void executeLockAction(UserLockTask task)
    {
        UserEntity user = userRepository.findByKeycloakId(task.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with k-id=%s not found".formatted(task.userId())
                ));
        boolean newActivityState = (task.action() == LockAction.UNLOCK);

        user.getBackground().setActivity(newActivityState);
        keycloakService.changeUserActivity(task.userId(), newActivityState);
    }

    @Recover
    public void recover(ConnectException e, UserLockTask task)
    {
        log.error("Failed to lock/unlock user with id={} after retries",
                task.userId(), e);
    }
}