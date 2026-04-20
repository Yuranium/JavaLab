package com.javalab.executionservice.controller;

import com.javalab.executionservice.models.dto.ExecutionStatusDto;
import com.javalab.executionservice.service.ExecutionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/execution")
@RequiredArgsConstructor
public class ExecutionController
{
    private final ExecutionService executionService;

//    @GetMapping("/check-status")
//    public ResponseEntity<@NonNull ExecutionStatusDto> checkStatus(
//            @RequestParam Long taskId, @AuthenticationPrincipal Jwt jwt
//    )
//    {
//        return new ResponseEntity<>(
//                executionService.checkStatus(taskId, jwt.getSubject()),
//                HttpStatus.OK
//        );
//    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @DeleteMapping("/cancel")
    public void cancelExecution(
            @RequestParam Long taskId, @AuthenticationPrincipal Jwt jwt
    )
    {
        //executionService.cancelExecution(taskId, jwt.getSubject());
    }
}