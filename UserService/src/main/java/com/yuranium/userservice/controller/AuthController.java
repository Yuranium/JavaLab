package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.UserLoginDto;
import com.yuranium.userservice.service.UserService;
import com.yuranium.userservice.util.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader)
    {
        return new ResponseEntity<>(
                Map.of("isValid", jwtUtil.isValidToken(authHeader)),
                HttpStatus.OK
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> createJwtToken(@RequestBody UserLoginDto userLogin)
    {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLogin.username(), userLogin.password()
        ));

        return new ResponseEntity<>(
                Map.of("token", jwtUtil.generateToken(
                        userService.getUserByUsername(userLogin.username()))
                ),
                HttpStatus.OK
        );
    }
}