package com.javalab.taskservice.controller;

import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.service.TestCaseService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/task")
public class TestCaseController
{
    private final TestCaseService testCaseService;

    @PatchMapping("/{taskId}/test-case/{testCaseId}")
    public ResponseEntity<@NonNull TestCaseResponseDto> updateTestCase(
            @PathVariable Long taskId, @PathVariable Long testCaseId, @RequestParam Long userId
    )
    {
        return new ResponseEntity<>(
                testCaseService.updateTestCase(taskId, testCaseId, userId),
                HttpStatus.OK
        );
    }
}