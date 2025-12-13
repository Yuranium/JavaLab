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
                .body(createExceptionBody(HttpStatus.NOT_FOUND, exc.getMessage()));
    }

    @ExceptionHandler(UserEntityNotCreatedException.class)
    public ResponseEntity<ExceptionBody> handleException(UserEntityNotCreatedException exc)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createExceptionBody(HttpStatus.INTERNAL_SERVER_ERROR, exc.getMessage()));
    }

    @ExceptionHandler(PasswordMissingException.class)
    public ResponseEntity<ExceptionBody> handleException(PasswordMissingException exc)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createExceptionBody(HttpStatus.UNAUTHORIZED, exc.getMessage()));
    }

    @ExceptionHandler(ConfirmationCodeNotFoundException.class)
    public ResponseEntity<ExceptionBody> handleException(ConfirmationCodeNotFoundException exc)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createExceptionBody(HttpStatus.NOT_FOUND, exc.getMessage()));
    }

    @ExceptionHandler(ConfirmationCodeExpiredException.class)
    public ResponseEntity<ExceptionBody> handleException(ConfirmationCodeExpiredException exc)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createExceptionBody(HttpStatus.BAD_REQUEST, exc.getMessage()));
    }

    private ExceptionBody createExceptionBody(HttpStatus status, String message)
    {
        return new ExceptionBody(status, LocalDateTime.now(), message);
    }
}