package com.javalab.taskservice.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
public class ExceptionBody implements Serializable
{
    private final Integer status;

    private final LocalDateTime timestamp;

    private final String message;

    public ExceptionBody(HttpStatus status, String message)
    {
        this.status = status.value();
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }
}