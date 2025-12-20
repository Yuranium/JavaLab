package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.dto.response.TaskDetailedResponseDto;
import com.javalab.taskservice.dto.response.TaskResponseDto;
import com.javalab.taskservice.mapper.TaskMapper;
import com.javalab.taskservice.repository.TaskRepository;
import com.javalab.taskservice.tables.records.TaskRecord;
import com.javalab.taskservice.util.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TaskService
{
    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final CategoryService categoryService;

    private final StarterCodeService starterCodeService;

    private final TestCaseService testCaseService;

    public Collection<TaskResponseDto> getAllTasks(Integer page, Integer size)
    {
        return taskRepository.getAllTasks(page, size)
                .map(taskMapper::toResponseDto);
    }

    public TaskDetailedResponseDto getTask(Long id)
    {
        return taskRepository.getDetailedTask(id);
    }

    public TaskResponseDto createTask(TaskRequestDto taskDto)
    {
        TaskRecord savedTask = taskRepository.saveTask(taskDto);
        categoryService.saveCategoryForTask(savedTask.getIdTask(), taskDto.categories());
        starterCodeService.createStarterCodeForTask(savedTask.getIdTask(), taskDto.starterCode());
        testCaseService.createTestCaseForTask(savedTask.getIdTask(), taskDto.testCases());

        return taskRepository.getTask(savedTask.getIdTask());
    }

    public TaskResponseDto updateTask(Long id, TaskRequestDto taskDto)
    {
        return taskMapper.toResponseDto(taskRepository
                .updateTask(id, taskDto)
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "Task with id=%d not found".formatted(id)
                        )
                )
        );
    }

    public void deleteTask(Long id)
    {
        taskRepository.deleteTask(id)
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "Task with id=%d not found".formatted(id)
                        )
                );
    }
}