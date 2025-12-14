package com.javalab.taskservice.util.exception;

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
                .body(new ExceptionBody(HttpStatus.NOT_FOUND, exc.getMessage()));
    }
}