package com.javalab.taskservice.controller;

import com.javalab.taskservice.dto.TaskRequestDto;
import com.javalab.taskservice.dto.TaskResponseDto;
import com.javalab.taskservice.service.TaskService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/task")
public class TaskController
{
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<@NonNull Iterable<TaskResponseDto>> getAllTasks(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    )
    {
        return new ResponseEntity<>(
                taskService.getAllTasks(page, size),
                HttpStatus.OK
        );
    }

    //    @GetMapping("/{id}")
//    public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long id)
//    {
//        return new ResponseEntity<>(
//                taskService.getTask(id),
//                HttpStatus.OK
//        );
//    }
//
    @PostMapping
    public ResponseEntity<@NonNull TaskResponseDto> createTask(@RequestBody TaskRequestDto taskDto)
    {
        return new ResponseEntity<>(
                taskService.createTask(taskDto),
                HttpStatus.CREATED
        );
    }
//
//    @PatchMapping("/{id}")
//    public ResponseEntity<TaskResponseDto> updateTask(
//            @PathVariable Long id,
//            @ModelAttribute TaskUpdateDto taskDto
//    )
//    {
//        return new ResponseEntity<>(
//                taskService.updateTask(id, taskDto),
//                HttpStatus.OK
//        );
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteTask(@PathVariable Long id)
//    {
//        taskService.deleteTask(id);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
}