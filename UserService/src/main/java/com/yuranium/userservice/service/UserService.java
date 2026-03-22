package com.yuranium.userservice.service;

import com.javalab.core.events.UserRegisteredEvent;
import com.javalab.core.exception.ResourceAlreadyExistsException;
import com.javalab.core.exception.ResourceNotCreatedException;
import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserUpdateDto;
import com.yuranium.userservice.models.entity.UserBackgroundEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.models.entity.UserIdempotencyEntity;
import com.yuranium.userservice.repository.UserBackgroundRepository;
import com.yuranium.userservice.repository.UserIdempotencyRepository;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final FileService fileService;

    private final KeycloakService keycloakService;

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
    public Iterable<String> getEmailsForNotify(PageRequest pageRequest)
    {
        return userRepository.findSuitableEmails(pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long id)
    {
        return userMapper.toResponseDto(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(UUID id)
    {
        return userMapper.toResponseDto(
                userRepository.findByKeycloakId(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "User with kc-id=%s not found.".formatted(id)
                        ))
        );
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userDto, UUID idempotencyKey)
    {
        if (idempotencyRepository.existsById(idempotencyKey))
            throw new ResourceAlreadyExistsException(
                    "The user with this id-key=%s already exists.".formatted(idempotencyKey)
            );
        idempotencyRepository.save(new UserIdempotencyEntity(idempotencyKey));

        String uploadedAvatarUrl = fileService.uploadFile(userDto.avatar());
        UUID keycloakUserId = null;
        try
        {
            keycloakUserId = keycloakService.createUser(userDto);
            UserEntity savedUser = saveUserWithRelations(userDto, uploadedAvatarUrl, keycloakUserId);
            Integer confirmCode = authService.generateAuthCode();
            kafkaSender.sendUserRegisteredEvent(new UserRegisteredEvent(
                    savedUser.getId(), savedUser.getUsername(),
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
        UserEntity userEntity = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with kc-id=%s not found.".formatted(keycloakId)
                ));
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
    public void updateLastLogin(UUID keycloakId, LocalDateTime loginTime)
    {
        UserEntity userEntity = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with kc-id=%s not found.".formatted(keycloakId)
                ));
        userEntity.getBackground().setLastLogin(loginTime);
    }

    @Transactional
    public void deleteUser(Long id)
    {
        UserEntity userEntity = findByIdOrThrow(id);
        fileService.deleteFile(userEntity.getAvatar());
        keycloakService.deleteUser(userEntity.getKeycloakId());
        userRepository.deleteById(id);
    }

    private UserEntity findByIdOrThrow(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ));
    }
}