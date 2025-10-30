package com.yuranium.userservice.util.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ExceptionBody(
        HttpStatus status,

        LocalDateTime timestamp,

        String message
) {}