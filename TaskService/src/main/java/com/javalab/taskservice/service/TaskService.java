package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.TaskRequestDto;
import com.javalab.taskservice.dto.TaskResponseDto;
import com.javalab.taskservice.mapper.TaskMapper;
import com.javalab.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService
{
    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    public Iterable<TaskResponseDto> getAllTasks(Integer page, Integer size)
    {
        return taskRepository.getAllTasks(page, size)
                .map(taskMapper::toResponseDto);
    }

    public TaskResponseDto createTask(TaskRequestDto taskDto)
    {
        return taskMapper.toResponseDto(
                taskRepository.saveTask(taskDto)
        );
    }
}