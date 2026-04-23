package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
import com.javalab.taskservice.dao.StarterCodeDao;
import com.javalab.taskservice.dao.TaskDao;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StarterCodeService
{
    private final StarterCodeDao starterCodeDao;

    private final TaskDao taskDao;

    public StarterCodeResponseDto createStarterCodeForTask(
            Long taskId, StarterCodeRequestDto starterCodeRequestDto
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        return starterCodeDao.createStarterCodeForTask(taskId, starterCodeRequestDto);
    }

    public StarterCodeResponseDto updateStarterCode(
            Long taskId, StarterCodeRequestDto requestDto
    )
    {
        if (taskDao.existsById(taskId))
            return starterCodeDao.updateStarterCode(taskId, requestDto);

        throw new ResourceNotFoundException(
                "The task with id=%d not found".formatted(taskId)
        );
    }
}