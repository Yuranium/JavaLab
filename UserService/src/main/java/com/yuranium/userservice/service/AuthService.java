package com.yuranium.userservice.service;

import com.javalab.core.events.UserRegisteredEvent;
import com.yuranium.userservice.models.entity.ConfirmationCodeEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.ConfirmCodeRepository;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import com.yuranium.userservice.util.exception.ConfirmationCodeExpiredException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService
{
    private final UserRepository userRepository;

    private final ConfirmCodeRepository codeRepository;

    private final KafkaSender kafkaSender;

    private final KeycloakService keycloakService;

    @Value("${auth.confirmation-code.lifetime}")
    private Duration codeLifetime;

    @Transactional
    public void createConfirmCode(Long userId, Integer confirmCode)
    {
        ConfirmationCodeEntity confirmEntity = new ConfirmationCodeEntity();
        confirmEntity.setUserId(userId);
        confirmEntity.setCode(confirmCode);
        codeRepository.save(confirmEntity);
    }

    @Transactional
    public void sendConfirmCode(UserRegisteredEvent event)
    {
        UserEntity userEntity = userRepository.findById(event.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(event.id())
                ));

        codeRepository.deleteAllByUserId(event.id());
        Integer confirmCode = generateAuthCode();
        createConfirmCode(event.id(), confirmCode);
        kafkaSender.sendUserRegisteredEvent(new UserRegisteredEvent(
                event.id(), userEntity.getKeycloakId(),
                event.username(), event.email(), confirmCode
        ));
    }

    @Transactional
    public void verifyAccount(String username, Integer code)
    {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with username=%s not found".formatted(username)
                ));
        ConfirmationCodeEntity confirmCode = codeRepository
                .findByUserIdAndCode(userEntity.getId(), code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The confirm code for userId=%d was not found"
                                .formatted(userEntity.getId())
                ));
        if (isCodeActive(confirmCode))
        {
            userEntity.getBackground().setActivity(true);
            userEntity.getBackground().setLastLogin(Instant.now());
            keycloakService.changeEmailStatus(userEntity.getKeycloakId(), true);
            codeRepository.delete(confirmCode);
        }
        else throw new ConfirmationCodeExpiredException(
                "The confirm code expired. Code lifetime is %s".formatted(codeLifetime)
        );
    }

    private Boolean isCodeActive(ConfirmationCodeEntity code)
    {
        return code.getCreatedDate().plus(codeLifetime).isAfter(Instant.now());
    }

    public Integer generateAuthCode()
    {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}