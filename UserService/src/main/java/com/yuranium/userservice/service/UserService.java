package com.yuranium.userservice.service;

import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.util.exception.UserEntityNotCreatedException;
import com.yuranium.userservice.util.exception.UserEntityNotFoundException;
import com.yuranium.userservice.mapper.UserMapper;
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
    private final FileService fileService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Iterable<UserResponseDto> getUsers(PageRequest pageRequest)
    {
        return userMapper.toResponseDto(
                userRepository.findAll(pageRequest).getContent()
        );
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long id)
    {
        return userMapper.toResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ))
        );
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userDto)
    {
        String uploadedAvatarUrl = null;
        try
        {
            uploadedAvatarUrl = fileService.uploadFile(userDto.avatar());

            UserEntity userEntity = userMapper.toEntity(userDto);
            userEntity.setAvatar(uploadedAvatarUrl);
            UserEntity savedUser = userRepository.save(userEntity);

            return userMapper.toResponseDto(savedUser);
        } catch (Exception exc)
        {
            if (uploadedAvatarUrl != null)
                fileService.deleteFile(uploadedAvatarUrl);
            throw new UserEntityNotCreatedException(exc.getMessage());
        }
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userDto) // todo
    {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ));

        return userMapper.toResponseDto(
                userRepository.save(userMapper.toEntity(userDto))
        );
    }

    @Transactional
    public void deleteUser(Long id)
    {
        userRepository.deleteById(id);
    }
}