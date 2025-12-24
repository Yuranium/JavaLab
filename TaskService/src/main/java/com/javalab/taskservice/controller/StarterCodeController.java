package com.javalab.taskservice.controller;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
import com.javalab.taskservice.service.StarterCodeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/task")
public class StarterCodeController
{
    private final StarterCodeService starterCodeService;

    @GetMapping("/{taskId}/starter-code")
    public ResponseEntity<@NonNull StarterCodeResponseDto> getStarterCode(
            @PathVariable Long taskId
    )
    {
        return new ResponseEntity<>(
                starterCodeService.getStarterCode(taskId),
                HttpStatus.OK
        );
    }

    @PatchMapping("/{taskId}/starter-code")
    public ResponseEntity<@NonNull StarterCodeResponseDto> updateStarterCode(
            @PathVariable Long taskId, @RequestBody StarterCodeRequestDto starterCodeRequestDto
    )
    {
        return new ResponseEntity<>(
                starterCodeService.updateStarterCode(taskId, starterCodeRequestDto),
                HttpStatus.OK
        );
    }
}