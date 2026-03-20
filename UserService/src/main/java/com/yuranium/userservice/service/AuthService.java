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
import java.time.LocalDateTime;
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
        if (userRepository.findById(event.id()).isPresent())
        {
            Integer confirmCode = generateAuthCode();
            createConfirmCode(event.id(), confirmCode);
            kafkaSender.sendUserRegisteredEvent(new UserRegisteredEvent(
                    event.id(), event.username(), event.email(), confirmCode
            ));
        }
        else throw new ResourceNotFoundException(
                "User with id=%d not found.".formatted(event.id())
        );
    }

    @Transactional
    public Boolean verifyAccount(Long userId, Integer code)
    {
        ConfirmationCodeEntity confirmCode = codeRepository
                .findByUserIdAndCode(userId, code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The confirm code for userId=%d was not found.".formatted(userId)
                ));

        if (isCodeActive(confirmCode))
        {
            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User with id=%d not found.".formatted(userId)
                    ));
            userEntity.getBackground().setActivity(true);
            userEntity.getBackground().setLastLogin(LocalDateTime.now());
            keycloakService.verifyUser(userEntity.getKeycloakId());
            codeRepository.delete(confirmCode);
            return true;
        }
        else throw new ConfirmationCodeExpiredException(
                "The confirm code expired. Code lifetime is %s".formatted(codeLifetime)
        );
    }

    private Boolean isCodeActive(ConfirmationCodeEntity code)
    {
        return code.getCreatedDate().plus(codeLifetime).isAfter(LocalDateTime.now());
    }

    public Integer generateAuthCode()
    {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}