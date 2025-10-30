package com.yuranium.userservice.models.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserRequestDto(
        String username,

        String name,

        String lastName,

        String password,

        String email,

        MultipartFile avatar
) {}