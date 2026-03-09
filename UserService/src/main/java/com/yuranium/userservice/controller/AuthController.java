package com.yuranium.userservice.controller;

import com.yuranium.javalabcore.UserRegisteredEvent;
import com.yuranium.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
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
    private final AuthService authService;

    @PostMapping("/send-confirmation-code")
    public ResponseEntity<Integer> createConfirmationCode(@RequestBody UserRegisteredEvent event)
    {
        authService.sendConfirmCode(event);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/verify-account")
    public ResponseEntity<?> validateConfirmationCode(@RequestParam Long userId, @RequestParam Integer code)
    {
        return new ResponseEntity<>(
                Map.of("accountVerified", authService.verifyAccount(userId, code)),
                HttpStatus.OK
        );
    }
}