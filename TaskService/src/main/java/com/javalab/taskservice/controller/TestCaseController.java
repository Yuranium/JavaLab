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
    public ResponseEntity<@NonNull TestCaseResponseDto> createTestCase(
            @PathVariable Long taskId, @RequestBody TestCaseRequestDto testCaseDto
    )
    {
        return new ResponseEntity<>(
                testCaseService.createTestCase(taskId, testCaseDto),
                HttpStatus.OK);
    }

    @PatchMapping("/{taskId}/test-case/{testCaseId}")
    public ResponseEntity<@NonNull TestCaseResponseDto> updateTestCase(
            @PathVariable Long taskId,
            @PathVariable Long testCaseId,
            @RequestBody TestCaseRequestDto testCaseRequestDto
    )
    {
        return new ResponseEntity<>(
                testCaseService.updateTestCase(
                        taskId, testCaseId, testCaseRequestDto
                ),
                HttpStatus.OK
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{taskId}/test-case/{testCaseId}")
    public void deleteTestCase(@PathVariable Long taskId, @PathVariable Long testCaseId)
    {
        testCaseService.deleteTestCase(taskId, testCaseId);
    }
}