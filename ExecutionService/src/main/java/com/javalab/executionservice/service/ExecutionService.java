package com.javalab.executionservice.service;

import com.javalab.executionservice.models.dao.ExecutionDao;
import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.util.ExecutionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExecutionService
{
    private final ExecutionDao executionDao;

    private final DockerContainerService dockerService;

    private final ExecutionValidator validator;

    @Async
    public CompletableFuture<?> executeCode(ExecutionRequestDto executionRequestDto)
    {
    }
}
