package com.yuranium.userservice.service;

import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.repository.UserBackgroundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBackgroundService
{
    private final UserBackgroundRepository userBackgroundRepository;

    private final UserMapper userMapper;

    public UserBackgroundResponseDto saveUserBackground(UserRequestDto requestDto)
    {
        return us
    }
}