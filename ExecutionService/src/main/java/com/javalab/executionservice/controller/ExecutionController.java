package com.javalab.executionservice.controller;

import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.dto.ExecutionResponseDto;
import com.javalab.executionservice.service.ExecutionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/execution")
@RequiredArgsConstructor
public class ExecutionController
{
    private final ExecutionService executionService;

    @PostMapping("execute")
    public ResponseEntity<@NonNull ExecutionResponseDto> executeCode(
            @RequestBody ExecutionRequestDto requestDto
            )
    {
        return new ResponseEntity<>(
                executionService.executeCode(requestDto),
                HttpStatus.OK
        );
    }
}