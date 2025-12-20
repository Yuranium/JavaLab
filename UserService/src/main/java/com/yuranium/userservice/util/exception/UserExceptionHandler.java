package com.yuranium.userservice.util.exception;

import com.yuranium.javalabcore.ExceptionBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class UserExceptionHandler
{
    @ExceptionHandler(UserEntityNotFoundException.class)
    public ResponseEntity<ExceptionBody> handleException(UserEntityNotFoundException exc)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionBody(HttpStatus.NOT_FOUND.value(), exc.getMessage()));
    }

    @ExceptionHandler(UserEntityNotCreatedException.class)
    public ResponseEntity<ExceptionBody> handleException(UserEntityNotCreatedException exc)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), exc.getMessage()));
    }

    @ExceptionHandler(PasswordMissingException.class)
    public ResponseEntity<ExceptionBody> handleException(PasswordMissingException exc)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionBody(HttpStatus.UNAUTHORIZED.value(), exc.getMessage()));
    }
}