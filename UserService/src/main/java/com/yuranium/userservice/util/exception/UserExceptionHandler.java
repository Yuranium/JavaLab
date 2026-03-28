package com.yuranium.userservice.util.exception;

import com.javalab.core.ExceptionBody;
import com.javalab.core.exception.ResourceAlreadyExistsException;
import com.javalab.core.exception.ResourceNotCreatedException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler
{
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionBody> handleException(ResourceNotFoundException exc)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionBody(HttpStatus.NOT_FOUND.value(), exc.getMessage()));
    }

    @ExceptionHandler(ResourceNotCreatedException.class)
    public ResponseEntity<ExceptionBody> handleException(ResourceNotCreatedException exc)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), exc.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ExceptionBody> handleException(ResourceAlreadyExistsException exc)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionBody(HttpStatus.CONFLICT.value(), exc.getMessage()));
    }

    @ExceptionHandler(PasswordMissingException.class)
    public ResponseEntity<ExceptionBody> handleException(PasswordMissingException exc)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionBody(HttpStatus.UNAUTHORIZED.value(), exc.getMessage()));
    }

    @ExceptionHandler({ConfirmationCodeExpiredException.class, IllegalArgumentException.class})
    public ResponseEntity<ExceptionBody> handleException(Exception exc)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionBody(HttpStatus.BAD_REQUEST.value(), exc.getMessage()));
    }
}