package com.yuranium.userservice.models.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

public record UserUpdateDto(
        String name,

        String lastName,

        MultipartFile avatar

) implements Serializable {}