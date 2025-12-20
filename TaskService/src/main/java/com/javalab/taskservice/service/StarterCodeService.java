package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
import com.javalab.taskservice.mapper.StarterCodeMapper;
import com.javalab.taskservice.repository.StarterCodeRepository;
import com.javalab.taskservice.repository.TaskRepository;
import com.javalab.taskservice.util.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StarterCodeService
{
    private final StarterCodeRepository starterCodeRepository;

    private final StarterCodeMapper starterCodeMapper;

    private final TaskRepository taskRepository;

    public StarterCodeResponseDto getStarterCode(Long taskId)
    {
        return starterCodeRepository.getStarterCode(taskId);
    }

    public StarterCodeResponseDto createStarterCodeForTask(
            Long taskId, StarterCodeRequestDto starterCodeRequestDto
    )
    {
        return starterCodeMapper.toResponseDto(
                starterCodeRepository.createStarterCodeForTask(taskId, starterCodeRequestDto)
        );
    }

    public StarterCodeResponseDto updateStarterCode(
            Long taskId, StarterCodeRequestDto requestDto
    )
    {
        if (taskRepository.getOnlyTask(taskId).isPresent())
            return starterCodeRepository.updateStarterCode(taskId, requestDto);

        throw new TaskNotFoundException("The task with id=%d not found".formatted(taskId));
    }
}