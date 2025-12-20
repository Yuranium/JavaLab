package com.javalab.taskservice.controller;

import com.javalab.taskservice.dto.request.TestCaseRequestDto;
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

    @PostMapping("/{taskId}/test-case")
    public ResponseEntity<@NonNull TestCaseResponseDto> createTestCaseForTask(
            @PathVariable Long taskId, @RequestBody TestCaseRequestDto testCaseDto
    )
    {
        return new ResponseEntity<>(
                testCaseService.createTestCaseForTask(taskId, testCaseDto),
                HttpStatus.OK);
    }

    @PatchMapping("/test-case/{testCaseId}")
    public ResponseEntity<@NonNull TestCaseResponseDto> updateTestCase(
            @PathVariable Long testCaseId,
            @RequestBody TestCaseRequestDto testCaseRequestDto
    )
    {
        return new ResponseEntity<>(
                testCaseService.updateTestCase(testCaseId, testCaseRequestDto),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/test-case/{testCaseId}")
    public ResponseEntity<?> deleteTestCase(@PathVariable Long testCaseId)
    {
        testCaseService.deleteTestCase(testCaseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}