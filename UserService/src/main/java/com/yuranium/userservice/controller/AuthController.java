package com.yuranium.userservice.controller;

import com.javalab.core.events.UserRegisteredEvent;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserUpdateDto;
import com.yuranium.userservice.service.AuthService;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController
{
    private final AuthService authService;

    private final UserService userService;

    @PostMapping("/send-confirmation-code")
    public ResponseEntity<Integer> createConfirmationCode(
            @RequestBody UserRegisteredEvent event
    )
    {
        authService.sendConfirmCode(event);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{username}/verify-account")
    public void validateConfirmationCode(
            @PathVariable String username, @RequestParam Integer code
    )
    {
        authService.verifyAccount(username, code);
    }

    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal Jwt jwt)
    {
        return new ResponseEntity<>(
                userService.getUser(UUID.fromString(jwt.getSubject())),
                HttpStatus.OK
        );
    }

    @PatchMapping
    public ResponseEntity<UserResponseDto> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @ModelAttribute UserUpdateDto userDto
    )
    {
        return new ResponseEntity<>(
                userService.updateUser(
                        UUID.fromString(jwt.getSubject()),
                        userDto
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal Jwt jwt)
    {
        userService.deleteUser(UUID.fromString(jwt.getSubject()));
    }
}