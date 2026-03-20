package com.javalab.taskservice.util.exception;

import com.javalab.core.ExceptionBody;
import com.javalab.core.exception.ResourceAlreadyExistsException;
import lombok.NonNull;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskExceptionHandler
{
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<@NonNull ExceptionBody> handleResourceNotFoundException(
            ResourceNotFoundException exc
    )
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionBody(HttpStatus.NOT_FOUND.value(), exc.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<@NonNull ExceptionBody> handleIllegalArgumentException(
            IllegalArgumentException exc
    )
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionBody(HttpStatus.BAD_REQUEST.value(), exc.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<@NonNull ExceptionBody> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException exc
    )
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionBody(HttpStatus.BAD_REQUEST.value(), exc.getMessage()));
    }
}