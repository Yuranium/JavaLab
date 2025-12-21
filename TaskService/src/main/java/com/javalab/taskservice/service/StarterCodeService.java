package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
import com.javalab.taskservice.repository.StarterCodeRepository;
import com.javalab.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StarterCodeService
{
    private final StarterCodeRepository starterCodeRepository;

    private final TaskRepository taskRepository;

    public StarterCodeResponseDto getStarterCode(Long taskId)
    {
        if (taskRepository.getOnlyTask(taskId).isPresent())
            return starterCodeRepository.getStarterCode(taskId);

        throw new ResourceNotFoundException("The task with id=%d not found".formatted(taskId));
    }

    public StarterCodeResponseDto createStarterCodeForTask(
            Long taskId, StarterCodeRequestDto starterCodeRequestDto
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        return starterCodeRepository.createStarterCodeForTask(taskId, starterCodeRequestDto);
    }

    public StarterCodeResponseDto updateStarterCode(
            Long taskId, StarterCodeRequestDto requestDto
    )
    {
        if (taskRepository.getOnlyTask(taskId).isPresent())
            return starterCodeRepository.updateStarterCode(taskId, requestDto);

        throw new ResourceNotFoundException(
                "The task with id=%d not found".formatted(taskId)
        );
    }
}