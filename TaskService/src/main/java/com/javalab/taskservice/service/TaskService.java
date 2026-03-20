package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.dto.response.CategoryResponseDto;
import com.javalab.taskservice.dto.response.TaskDetailedResponseDto;
import com.javalab.taskservice.dto.response.TaskResponseDto;
import com.javalab.taskservice.dto.response.TaskUpdatedResponseDto;
import com.javalab.taskservice.repository.TaskRepository;
import com.javalab.taskservice.service.kafka.KafkaSender;
import com.javalab.taskservice.tables.records.TaskRecord;
import com.javalab.core.events.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TaskService
{
    private final TaskRepository taskRepository;

    private final CategoryService categoryService;

    private final StarterCodeService starterCodeService;

    private final TestCaseService testCaseService;

    private final KafkaSender kafkaSender;

    public Collection<TaskResponseDto> getAllTasks(Integer page, Integer size)
    {
        return taskRepository.getAllTasks(page, size);
    }

    public TaskDetailedResponseDto getTask(Long id)
    {
        return taskRepository.getDetailedTask(id);
    }

    public TaskResponseDto createTask(TaskRequestDto taskDto)
    {
        TaskRecord savedTask = taskRepository.saveTask(taskDto);
        var categories = categoryService.saveCategoryForTask(savedTask.getIdTask(), taskDto.categories());
        starterCodeService.createStarterCodeForTask(savedTask.getIdTask(), taskDto.starterCode());
        testCaseService.createTestCasesForTask(savedTask.getIdTask(), taskDto.testCases());
        kafkaSender.sendTaskCreatedEvent(
                new TaskCreatedEvent(
                        savedTask.getTitle(),
                        savedTask.getDifficulty(),
                        categories.stream()
                                .map(CategoryResponseDto::title)
                                .toList()
                )
        );

        return taskRepository.getTask(savedTask.getIdTask());
    }

    public TaskUpdatedResponseDto updateTask(Long id, TaskRequestDto taskDto)
    {
        return taskRepository
                .updateTask(id, taskDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "The task with id=%d not found".formatted(id)
                        )
                );
    }

    public void deleteTask(Long id)
    {
        taskRepository.deleteTask(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "The task with id=%d not found".formatted(id)
                        )
                );
    }
}