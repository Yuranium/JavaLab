package com.yuranium.userservice.controller;

import com.yuranium.userservice.util.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private final JwtUtil jwtUtil;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader)
    {
        return new ResponseEntity<>(
                Map.of("isValid", jwtUtil.isValidToken(authHeader)),
                HttpStatus.OK
        );
    }
}