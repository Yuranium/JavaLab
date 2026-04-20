package com.javalab.executionservice.service;

import com.javalab.executionservice.models.dao.ExecutionDao;
import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.dto.ExecutionResponseDto;
import com.javalab.executionservice.models.dto.ExecutionStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionService
{
    private final ExecutionDao executionDao;

    public ExecutionResponseDto createExecution(ExecutionRequestDto executionRequestDto)
    {
        return new ExecutionResponseDto();
    }

    public ExecutionStatusDto getStatus(Long taskId, String username)
    {
        return null;
    }
}
