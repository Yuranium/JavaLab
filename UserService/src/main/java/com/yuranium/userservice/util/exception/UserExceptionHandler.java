package com.yuranium.userservice.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class UserExceptionHandler
{
    @ExceptionHandler(UserEntityNotFoundException.class)
    public ResponseEntity<ExceptionBody> handleException(UserEntityNotFoundException exc)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionBody(HttpStatus.NOT_FOUND,
                        LocalDateTime.now(),
                        exc.getMessage())
                );
    }
}