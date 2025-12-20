package com.javalab.taskservice.util.exception;

import com.yuranium.javalabcore.ExceptionBody;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskExceptionHandler
{
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<@NonNull ExceptionBody> handleTaskNotFoundException(
            TaskNotFoundException exc
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
}