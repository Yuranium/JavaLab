package com.yuranium.userservice.service;

import com.yuranium.javalabcore.UserRegisteredEvent;
import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.CustomUserDetails;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserUpdateDto;
import com.yuranium.userservice.models.entity.AuthEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.kafka.KafkaSender;
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

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService
{
    private final FileService fileService;

    private final AuthService authService;

    private final UserRepository userRepository;

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

            Integer confirmCode = authService.generateAuthCode();
            kafkaSender.sendUserRegisteredEvent(new UserRegisteredEvent(
                    savedUser.getId(), savedUser.getUsername(),
                    savedUser.getEmail(), confirmCode
            ));

            authService.createConfirmCode(savedUser.getId(), confirmCode);
            return userMapper.toResponseDto(savedUser);
        } catch (Exception exc)
        {
            if (uploadedAvatarUrl != null)
                fileService.deleteFile(uploadedAvatarUrl);
            throw new UserEntityNotCreatedException(exc.getMessage());
        }
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userDto) // todo
    {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ));

        userEntity.setName(userDto.name());
        userEntity.setLastName(userDto.lastName());
        userEntity.setAvatar(fileService.updateFile(
                userEntity.getAvatar(), userDto.avatar())
        );

        return userMapper.toResponseDto(
                userRepository.save(userEntity)
        );
    }

    @Transactional
    public void deleteUser(Long id)
    {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserEntityNotFoundException(
                        "User with id=%d not found.".formatted(id)
                ));
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

    @Transactional
    public UserEntity loginToUserAccount(String username)
    {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                                "The user with username=%s was not found".formatted(username)
                        )
                );
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }
}