package com.yuranium.userservice.models.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserUpdateDto(
        String name,

        String lastName,

        MultipartFile avatar
) {}