package com.yuranium.userservice.service;

import com.javalab.core.events.UserLockedEvent;
import com.yuranium.userservice.enums.LockAction;
import com.yuranium.userservice.models.dto.userlock.UserUnlockRequest;
import com.yuranium.userservice.models.dto.userlock.UserLockRequest;
import com.yuranium.userservice.models.dto.userlock.UserLockTask;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import com.yuranium.userservice.util.UserLockActionTaskQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLockService
{
    private final UserRepository userRepository;

    private final KeycloakService keycloakService;

    private final KafkaSender kafkaSender;

    private final UserLockActionTaskQueue taskQueue;

    @Transactional
    @Retryable(
            retryFor = ConnectException.class,
            backoff = @Backoff(delay = 5000, multiplier = 2, maxDelay = 10000)
    )
    public void executeLockAction(UserLockTask task)
    {
        UserEntity user = userRepository.findById(task.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(task.userId())
                ));
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
    public void lockUser(String username, UserLockRequest duration)
    {
        if (!validLockDate(duration.startLock(), duration.endLock()))
            throw new IllegalArgumentException("Incorrect startLock or endLock time");

        UserEntity user = findByUsername(username);
        Instant start = duration.startLock() != null ? duration.startLock() : Instant.now();
        taskQueue.scheduleTask(user.getId(), LockAction.LOCK, start);

        if (duration.endLock() != null)
            taskQueue.scheduleTask(user.getId(), LockAction.UNLOCK, duration.endLock());

        sendLockEvent(user, duration.startLock(), duration.endLock(), duration.message());
    }

    /**
     * При разблокировке:
     * 1. Если введена дата разблокировки - разблокировать в эту дату
     * 2. Если ничего не введено - разблокировать сейчас
     */
    @Transactional
    public void unlockUser(String username, UserUnlockRequest request)
    {
        UserEntity user = findByUsername(username);
        Instant unlockTime = request.unlockTime() != null
                ? request.unlockTime()
                : Instant.now();

        if (!validUnlockDate(request.unlockTime()))
            throw new IllegalArgumentException("Unlock time cannot be in the past");

        taskQueue.scheduleTask(user.getId(), LockAction.UNLOCK, unlockTime);
        sendUnlockEvent(user, unlockTime);
    }

    private UserEntity findByUsername(String username)
    {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with username=%s not found".formatted(username))
                );
    }

    private void sendLockEvent(UserEntity user, Instant start, Instant end, String message)
    {
        String timezone = user.getBackground().getTimezone() == null
                ? "UTC" : user.getBackground().getTimezone();
        ZoneId zone = ZoneId.of(timezone);
        kafkaSender.sendUserLockedEvent(new UserLockedEvent(
                        user.getUsername(),
                        user.getEmail(),
                        start != null ? start.atZone(zone).toOffsetDateTime() : null,
                        end != null ? end.atZone(zone).toOffsetDateTime() : null,
                        true,
                        message
                )
        );
    }

    private void sendUnlockEvent(UserEntity user, Instant unlockTime)
    {
        String timezone = user.getBackground().getTimezone() == null
                ? "UTC" : user.getBackground().getTimezone();
        ZoneId zone = ZoneId.of(timezone);
        kafkaSender.sendUserLockedEvent(new UserLockedEvent(
                        user.getUsername(),
                        user.getEmail(),
                        null,
                        unlockTime != null ? unlockTime.atZone(zone).toOffsetDateTime() : null,
                        false,
                        null
                )
        );
    }

    private boolean validLockDate(Instant startLock, Instant endLock)
    {
        if (startLock == null || endLock == null)
            return true;
        Instant now = Instant.now();
        Instant earliestAllowed = now.minus(1, ChronoUnit.MINUTES);

        boolean sequenceValid = !endLock.isBefore(startLock);
        boolean startValid = !startLock.isBefore(earliestAllowed);
        boolean enoughDuration = Duration.between(startLock, endLock).getSeconds() > 60;

        return sequenceValid && startValid && enoughDuration;
    }

    private boolean validUnlockDate(Instant unlockTime)
    {
        if (unlockTime == null)
            return true;
        Duration inaccuracy = Duration.between(unlockTime, Instant.now());
        return inaccuracy.getSeconds() <= 60;
    }
}