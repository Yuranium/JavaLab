package com.yuranium.userservice;

import com.javalab.core.events.UserRegisteredEvent;
import com.yuranium.userservice.mapper.UserMapper;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.entity.UserBackgroundEntity;
import com.yuranium.userservice.models.entity.UserEntity;
import com.yuranium.userservice.models.entity.UserIdempotencyEntity;
import com.yuranium.userservice.repository.UserBackgroundRepository;
import com.yuranium.userservice.repository.UserIdempotencyRepository;
import com.yuranium.userservice.repository.UserRepository;
import com.yuranium.userservice.service.AuthService;
import com.yuranium.userservice.service.FileService;
import com.yuranium.userservice.service.UserService;
import com.yuranium.userservice.service.kafka.KafkaSender;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserTests
{
    @Mock
    private UserIdempotencyRepository idempotencyRepository;

    @Mock
    private FileService fileService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UserBackgroundRepository backgroundRepository;

    @Mock
    private KafkaSender kafkaSender;

    @InjectMocks
    private UserService userService;

    @Test
    @SneakyThrows
    void testCreateUserSuccess()
    {
        UUID idempotencyKey = UUID.randomUUID();
        MultipartFile avatarFile = createMultipartFile();

        UserRequestDto requestDto = createRequestDto(avatarFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("username");
        userEntity.setEmail("email");

        UserResponseDto responseDto = createResponseDto();
        String avatarUrl = "http://example.com/avatar.jpg";
        Integer confirmCode = 1234;

        when(idempotencyRepository.existsById(idempotencyKey)).thenReturn(false);
        when(fileService.uploadFile(requestDto.avatar())).thenReturn(avatarUrl);
        when(userMapper.toEntity(requestDto)).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(authService.generateAuthCode()).thenReturn(confirmCode);
        when(userMapper.toResponseDto(userEntity)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(requestDto, idempotencyKey);

        assertThat(result).isEqualTo(responseDto);

        verify(idempotencyRepository).existsById(idempotencyKey);
        verify(fileService).uploadFile(requestDto.avatar());
        verify(userMapper).toEntity(requestDto);
        verify(userRepository).save(userEntity);
        verify(backgroundRepository).save(any(UserBackgroundEntity.class));
        verify(kafkaSender).sendUserRegisteredEvent(any(UserRegisteredEvent.class));
        verify(authService).createConfirmCode(userEntity.getId(), confirmCode);
        verify(idempotencyRepository).save(any(UserIdempotencyEntity.class));

        verify(fileService, never()).deleteFile(anyString());
    }

    private UserResponseDto createResponseDto()
    {
        return new UserResponseDto(
                1L,
                "username",
                "John",
                "Doe",
                LocalDateTime.now(),
                null,
                false,
                true,
                "Europe/Moscow"
        );
    }

    private UserRequestDto createRequestDto(MultipartFile avatarFile)
    {
        return new UserRequestDto(
                "username",
                "John",
                "Doe",
                "password",
                "john.doe@example.com",
                avatarFile,
                true,
                "Europe/Moscow"
        );
    }

    private MultipartFile createMultipartFile()
    {
        return new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }
}