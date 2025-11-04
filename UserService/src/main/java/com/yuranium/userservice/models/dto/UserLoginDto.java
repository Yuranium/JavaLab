package com.yuranium.userservice.models.dto;

public record UserLoginDto(
        String username,

        String password
) {}