package com.yuranium.userservice.service;

import com.yuranium.javalabcore.UserRegisteredEvent;
import com.yuranium.javalabcore.exception.ResourceAlreadyExistsException;
import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.CustomUserDetails;
import com.yuranium.userservice.models.dto.*;
import com.yuranium.userservice.models.entity.AuthEntity;
import com.yuranium.userservice.models.entity.UserBackgroundEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.models.entity.UserIdempotencyEntity;
import com.yuranium.userservice.repository.UserBackgroundRepository;
import com.yuranium.userservice.repository.UserIdempotencyRepository;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import com.yuranium.userservice.util.exception.PasswordMissingException;
import com.yuranium.userservice.util.exception.UnconfirmedAccountException;
import com.yuranium.userservice.util.exception.ResourceNotCreatedException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService
{
    private final FileService fileService;

    private final AuthService authService;

    private final UserRepository userRepository;

    private final UserIdempotencyRepository idempotencyRepository;

    private final UserBackgroundRepository backgroundRepository;

    private final UserMapper userMapper;

    private final KafkaSender kafkaSender;

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
        return userMapper.toResponseDto(findByIdOrThrow(id));
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userDto, UUID idempotencyKey)
    {
        String uploadedAvatarUrl = null;
        if (idempotencyRepository.existsById(idempotencyKey))
            throw new ResourceAlreadyExistsException(
                    "The user with this id-key=%s already exists.".formatted(idempotencyKey)
            );

        try
        {
            uploadedAvatarUrl = fileService.uploadFile(userDto.avatar());

            UserEntity userEntity = userMapper.toEntity(userDto);
            userEntity.setAvatar(uploadedAvatarUrl);
            UserEntity savedUser = userRepository.save(userEntity);
            authService.setAuthForLocalUser(savedUser, userDto);
            savedUser.setBackground(
                    backgroundRepository.save(
                            new UserBackgroundEntity(userDto.timezone(), savedUser)
                    )
            );

            Integer confirmCode = authService.generateAuthCode();
            kafkaSender.sendUserRegisteredEvent(new UserRegisteredEvent(
                    savedUser.getId(), savedUser.getUsername(),
                    savedUser.getEmail(), confirmCode
            ));

            authService.createConfirmCode(savedUser.getId(), confirmCode);
            idempotencyRepository.save(new UserIdempotencyEntity(idempotencyKey));
            return userMapper.toResponseDto(savedUser);
        } catch (Exception exc)
        {
            if (uploadedAvatarUrl != null)
                fileService.deleteFile(uploadedAvatarUrl);
            throw new ResourceNotCreatedException(exc.getMessage());
        }
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userDto)
    {
        UserEntity userEntity = findByIdOrThrow(id);
        userEntity.setAvatar(fileService.updateFile(
                userEntity.getAvatar(), userDto.avatar())
        );
        userMapper.updateEntity(userEntity, userDto);

        return userMapper.toResponseDto(
                userRepository.save(userEntity)
        );
    }

    @Transactional
    public void deleteUser(Long id)
    {
        UserEntity userEntity = findByIdOrThrow(id);
        fileService.deleteFile(userEntity.getAvatar());
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

        if (!user.getBackground().getActivity())
            throw new UnconfirmedAccountException(
                    "The user with this username=%s is disabled".formatted(username)
            );

        return new CustomUserDetails(user, getPassword(username, user));
    }

    private String getPassword(String username, UserEntity user)
    {
        return user.getAuthMethods().stream()
                .map(AuthEntity::getPassword)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new PasswordMissingException(
                                "No password found for username=%s".formatted(username)
                        )
                );
    }

    @Transactional
    public UserEntity loginToUserAccount(String username)
    {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                                "The user with username=%s was not found".formatted(username)
                        )
                );
        user.getBackground().setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    private UserEntity findByIdOrThrow(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ));
    }
}