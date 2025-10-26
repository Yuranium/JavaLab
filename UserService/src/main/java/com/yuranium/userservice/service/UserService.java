package com.yuranium.userservice.service;

import com.yuranium.userservice.util.exception.UserEntityNotFoundException;
import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.dto.UserDto;
import com.yuranium.userservice.models.dto.UserInputDto;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Iterable<UserDto> getUsers(PageRequest pageRequest)
    {
        return userMapper.toDto(
                userRepository.findAll(pageRequest).getContent()
        );
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id)
    {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ))
        );
    }

    @Transactional
    public UserDto createUser(UserInputDto userDto) // todo
    {
        UserEntity userEntity = userMapper.toEntity(userDto);
        return userMapper.toDto(userRepository.save(userEntity));
    }

    @Transactional
    public UserDto updateUser(Long id, UserInputDto userDto) // todo
    {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ));

        return userMapper.toDto(
                userRepository.save(userMapper.toEntity(userDto))
        );
    }

    @Transactional
    public void deleteUser(Long id)
    {
        userRepository.deleteById(id);
    }
}