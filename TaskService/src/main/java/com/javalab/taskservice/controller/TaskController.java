package com.javalab.taskservice.controller;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.dto.response.TaskAttributeResponseDto;
import com.javalab.taskservice.dto.response.TaskDetailedResponseDto;
import com.javalab.taskservice.dto.response.TaskResponseDto;
import com.javalab.taskservice.dto.response.TaskUpdatedResponseDto;
import com.javalab.taskservice.enums.DifficultyType;
import com.javalab.taskservice.enums.JavaCategory;
import com.javalab.taskservice.service.TaskService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/task")
public class TaskController
{
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<@NonNull Collection<TaskResponseDto>> getAllTasks(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    )
    {
        return new ResponseEntity<>(
                taskService.getAllTasks(page, size),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull TaskDetailedResponseDto> getTask(@PathVariable Long id)
    {
        return new ResponseEntity<>(
                taskService.getTask(id),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<@NonNull TaskResponseDto> createTask(
            @RequestBody TaskRequestDto taskDto
    )
    {
        return new ResponseEntity<>(
                taskService.createTask(taskDto),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<@NonNull TaskUpdatedResponseDto> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequestDto taskDto
    )
    {
        return new ResponseEntity<>(
                taskService.updateTask(id, taskDto),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id)
    {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/attributes")
    public ResponseEntity<?> getAttributes()
    {
        return new ResponseEntity<>(
                new TaskAttributeResponseDto(
                        List.of(DifficultyType.values()),
                        List.of(JavaCategory.values())
                ),
                HttpStatus.OK
        );
    }
}