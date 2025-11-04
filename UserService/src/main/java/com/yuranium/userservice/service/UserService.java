package com.yuranium.userservice.service;

import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.CustomUserDetails;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.entity.AuthEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.util.exception.PasswordMissingException;
import com.yuranium.userservice.util.exception.UserEntityNotCreatedException;
import com.yuranium.userservice.util.exception.UserEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService
{
    private final FileService fileService;

    private final AuthService authService;

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
            authService.setAuthForLocalUser(savedUser, userDto);

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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "The user with username=%s was not found".formatted(username)
                        )
                );
        return new CustomUserDetails(user, user.getAuthMethods().stream()
                .map(AuthEntity::getPassword)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new PasswordMissingException(
                        "No password found for username=%s".formatted(username)
                        )
                )
        );
    }

    @Transactional(readOnly = true)
    public UserEntity getUserByUsername(String username)
    {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "The user with username=%s was not found".formatted(username)
                        )
                );
    }
}