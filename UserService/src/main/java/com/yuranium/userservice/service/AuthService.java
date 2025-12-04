package com.yuranium.userservice.service;

import com.yuranium.javalabcore.UserRegisteredEvent;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.entity.AuthEntity;
import com.yuranium.userservice.models.entity.ConfirmationCodeEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.AuthRepository;
import com.yuranium.userservice.repository.ConfirmCodeRepository;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import com.yuranium.userservice.util.exception.ConfirmationCodeExpiredException;
import com.yuranium.userservice.util.exception.ConfirmationCodeNotFoundException;
import com.yuranium.userservice.util.exception.UserEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService
{
    private final PasswordEncoder passwordEncoder;

    private final AuthRepository authRepository;

    private final UserRepository userRepository;

    private final ConfirmCodeRepository codeRepository;

    private final KafkaSender kafkaSender;

    @Value("${auth.confirmation-code.lifetime}")
    private Duration codeLifetime;

    @Transactional
    public AuthEntity setAuthForLocalUser(UserEntity user, UserRequestDto userDto)
    {
        AuthEntity authEntity = new AuthEntity();
        authEntity.setPassword(passwordEncoder.encode(userDto.password()));
        authEntity.setUser(user);
        authRepository.save(authEntity);
        user.setAuthMethods(List.of(authEntity));
        return authEntity;
    }

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
        else throw new UserEntityNotFoundException(
                "User with id=%d not found.".formatted(event.id())
        );
    }

    @Transactional
    public Boolean verifyAccount(Long userId, Integer code)
    {
        ConfirmationCodeEntity confirmCode = codeRepository
                .findByUserIdAndCode(userId, code)
                .orElseThrow(() -> new ConfirmationCodeNotFoundException(
                        "The confirm code for userId=%d was not found.".formatted(userId)
                ));

        if (isCodeActive(confirmCode))
        {
            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new UserEntityNotFoundException(
                            "User with id=%d not found.".formatted(userId)
                    ));
            userEntity.setActivity(true);
            userEntity.setLastLogin(LocalDateTime.now());
            userRepository.save(userEntity);
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