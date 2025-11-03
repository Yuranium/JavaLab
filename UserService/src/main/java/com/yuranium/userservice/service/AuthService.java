package com.yuranium.userservice.service;

import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.entity.AuthEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService
{
    private final PasswordEncoder passwordEncoder;

    private final AuthRepository authRepository;

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
}