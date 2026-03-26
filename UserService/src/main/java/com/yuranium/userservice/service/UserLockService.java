package com.yuranium.userservice.service;

import com.javalab.core.events.UserLockedEvent;
import com.yuranium.userservice.enums.LockAction;
import com.yuranium.userservice.models.dto.userlock.UserLockDuration;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;
import java.time.Instant;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLockService
{
    private final UserRepository userRepository;

    private final KeycloakService keycloakService;

    private final KafkaSender kafkaSender;

    private final UserLockActionService actionService;

    @Transactional
    @Retryable(
            retryFor = ConnectException.class,
            backoff = @Backoff(delay = 5000, multiplier = 2, maxDelay = 10000)
    )
    public void executeLockAction(UserLockTask task)
    {
        UserEntity user = findUserById(task.userId());
        boolean shouldBeActive = (task.action() == LockAction.UNLOCK);

        user.getBackground().setActivity(shouldBeActive);
        keycloakService.changeUserActivity(user.getKeycloakId(), shouldBeActive);
    }

    @Recover
    public void recover(ConnectException e, UserLockTask task)
    {
        log.error("Failed to lock/unlock user with id={} after retries",
                task.userId(), e);
    }

    /**
     * При блокировке:
     * 1. Если введена начальная дата и конечная - планируется время блокировки
     * 2. Если введена только начальная дата - блокировка перманентная, начиная с начальной даты
     * 3. Если введена только конечаня дата - блокировка от текущей даты до конечной
     * 4. Если не введено ничего - блокировка перманента, начиная с текущей даты
     */
    @Transactional
    public void lockUser(Long id, UserLockDuration duration)
    {
        if (!validLockDate(duration.startLock(), duration.endLock()))
            throw new IllegalArgumentException("Incorrect startLock or endLock time");
        UserEntity user = findUserById(id);
        if (duration.startLock() == null && duration.endLock() == null)
            actionService.scheduleTask(id, LockAction.LOCK, Instant.now());
        if (duration.startLock() == null && duration.endLock() != null)
        {
            actionService.scheduleTask(id, LockAction.LOCK, Instant.now());
            actionService.scheduleTask(id, LockAction.UNLOCK, duration.endLock());
        }
        if (duration.startLock() != null && duration.endLock() == null)
            actionService.scheduleTask(id, LockAction.LOCK, duration.startLock());
        if (duration.startLock() != null && duration.endLock() != null)
        {
            actionService.scheduleTask(id, LockAction.LOCK, duration.startLock());
            actionService.scheduleTask(id, LockAction.UNLOCK, duration.endLock());
        }

        sendLockEvent(user, duration.startLock(), duration.endLock(), duration.message());
    }

    /**
     * При разблокировке:
     * 1. Если введена дата разблокировки - разблокировать в эту дату
     * 2. Если ничего не введено - разблокировать сейчас
     */
    @Transactional
    public void unlockUser(Long id, Instant unlockTime)
    {
        UserEntity user = findUserById(id);
        if (unlockTime != null && unlockTime.isBefore(Instant.now()))
            throw new IllegalArgumentException("The unlock time cannot be less than now");
        if (unlockTime == null)
            actionService.scheduleTask(id, LockAction.UNLOCK, Instant.now());
        else actionService.scheduleTask(id, LockAction.UNLOCK, unlockTime);
        sendUnlockEvent(user);
    }

    private UserEntity findUserById(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(id))
                );
    }

    private void sendLockEvent(UserEntity user, Instant start, Instant end, String message)
    {
        ZoneId zone = ZoneId.of(user.getBackground().getTimezone());
        kafkaSender.sendUserLockedEvent(new UserLockedEvent(
                        user.getUsername(),
                        user.getEmail(),
                        start.atZone(zone).toOffsetDateTime(),
                        end != null ? end.atZone(zone).toOffsetDateTime() : null,
                        true,
                        message
                )
        );
    }

    private void sendUnlockEvent(UserEntity user)
    {
        kafkaSender.sendUserLockedEvent(new UserLockedEvent(
                        user.getUsername(),
                        user.getEmail(),
                        null,
                        null,
                        false,
                        null
                )
        );
    }

    public boolean validLockDate(Instant startLock, Instant endLock)
    {
        if (startLock == null || endLock == null)
            return true;
        return !endLock.isBefore(startLock) || !endLock.isBefore(Instant.now());
    }
}