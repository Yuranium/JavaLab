package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserUpdateDto;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Iterable<UserResponseDto>> getUsers(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    )
    {
        return new ResponseEntity<>(
                userService.getUsers(PageRequest.of(page, size)),
                HttpStatus.OK
        );
    }

    @GetMapping("/internal/email-notification")
    public ResponseEntity<Iterable<String>> getEmails(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "30") Integer size
    )
    {
        return new ResponseEntity<>(
                userService.getEmails(PageRequest.of(page, size)),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal Jwt jwt)
    {
        return new ResponseEntity<>(
                userService.getUser(UUID.fromString(jwt.getSubject())),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id)
    {
        return new ResponseEntity<>(
                userService.getUser(id),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @ModelAttribute UserRequestDto userDto,
            @RequestHeader(value = "X-idempotency-key", required = false) UUID idempotencyKey
    )
    {
        if (idempotencyKey == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                userService.createUser(userDto, idempotencyKey),
                HttpStatus.CREATED
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id)
    {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}