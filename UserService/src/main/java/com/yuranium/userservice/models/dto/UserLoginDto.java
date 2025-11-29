package com.yuranium.userservice.models.dto;

import java.io.Serializable;

public record UserLoginDto(
        String username,

        String password

) implements Serializable {}