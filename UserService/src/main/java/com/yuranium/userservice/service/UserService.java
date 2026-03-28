package com.yuranium.userservice.service;

import com.javalab.core.events.UserRegisteredEvent;
import com.javalab.core.exception.ResourceAlreadyExistsException;
import com.javalab.core.exception.ResourceNotCreatedException;
import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.dto.*;
import com.yuranium.userservice.models.entity.UserBackgroundEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserBackgroundRepository;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static com.yuranium.userservice.util.data.UserSpecifications.hasActivity;
import static com.yuranium.userservice.util.data.UserSpecifications.hasNotifyEnabled;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final FileService fileService;

    private final KeycloakService keycloakService;

    private final AuthService authService;

    private final UserRepository userRepository;

    private final UserBackgroundRepository backgroundRepository;

    private final UserMapper userMapper;

    private final KafkaSender kafkaSender;

    @Transactional(readOnly = true)
    public Page<PublicUserResponseDto> getUsers(Pageable page, UserFilterDto filterDto)
    {
        Page<UserEntity> users = userRepository.findAll(
                hasActivity(filterDto.activity())
                        .and(hasNotifyEnabled(filterDto.notifyEnabled())),
                page
        );

        return users.map(userMapper::toPublicResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<String> getEmailsForNotify(Pageable page)
    {
        return userRepository.findSuitableEmails(page);
    }

    @Transactional(readOnly = true)
    public PublicUserResponseDto getUser(String username)
    {
        return userMapper.toPublicResponseDto(
                userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "User with username=%s not found".formatted(username)
                        ))
        );
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(UUID id)
    {
        return userMapper.toResponseDto(findByIdOrThrow(id));
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userDto)
    {
        if (userRepository.existsByUsernameOrEmail(userDto.username(), userDto.email()))
            throw new ResourceAlreadyExistsException(
                    "The user with this username or email already exists"
            );

        String uploadedAvatarUrl = fileService.uploadFile(userDto.avatar());
        UUID keycloakUserId = null;
        try
        {
            keycloakUserId = keycloakService.createUser(userDto);
            UserEntity savedUser = saveUserWithRelations(userDto, uploadedAvatarUrl, keycloakUserId);
            Integer confirmCode = authService.generateAuthCode();
            kafkaSender.sendUserRegisteredEvent(new UserRegisteredEvent(
                    savedUser.getId(), keycloakUserId, savedUser.getUsername(),
                    savedUser.getEmail(), confirmCode
            ));

            authService.createConfirmCode(savedUser.getId(), confirmCode);
            return userMapper.toResponseDto(savedUser);
        } catch (Exception exc)
        {
            if (keycloakUserId != null)
                keycloakService.deleteUser(keycloakUserId);
            if (uploadedAvatarUrl != null)
                fileService.deleteFile(uploadedAvatarUrl);
            throw new ResourceNotCreatedException(exc.getMessage());
        }
    }

    private UserEntity saveUserWithRelations(
            UserRequestDto userDto, String avatarUrl, UUID keycloakUserId
    )
    {
        UserEntity userEntity = userMapper.toEntity(userDto);
        userEntity.setAvatar(avatarUrl);
        userEntity.setKeycloakId(keycloakUserId);
        UserEntity savedUser = userRepository.save(userEntity);
        backgroundRepository.save(new UserBackgroundEntity(userDto, savedUser));
        return savedUser;
    }

    @Transactional
    public UserResponseDto updateUser(UUID keycloakId, UserUpdateDto userDto)
    {
        UserEntity userEntity = findByIdOrThrow(keycloakId);
        if (userDto.avatar() != null)
            userEntity.setAvatar(fileService.updateFile(
                    userEntity.getAvatar(), userDto.avatar())
            );
        userMapper.updateEntity(userEntity, userDto);

        return userMapper.toResponseDto(
                userRepository.save(userEntity)
        );
    }

    @Transactional
    public void updateLastLogin(UUID keycloakId, Instant loginTime)
    {
        UserEntity userEntity = findByIdOrThrow(keycloakId);
        userEntity.getBackground().setLastLogin(loginTime);
    }

    @Transactional
    public void deleteUser(UUID keycloakId)
    {
        UserEntity userEntity = findByIdOrThrow(keycloakId);
        fileService.deleteFile(userEntity.getAvatar());
        keycloakService.deleteUser(userEntity.getKeycloakId());
        userRepository.deleteByKeycloakId(keycloakId);
    }

    private UserEntity findByIdOrThrow(UUID keycloakId)
    {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with k-id=%s not found".formatted(keycloakId)
                ));
    }
}