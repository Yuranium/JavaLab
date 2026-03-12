package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "30", required = false) Integer size
    )
    {
        return new ResponseEntity<>(
                userService.getUsers(PageRequest.of(page, size)),
                HttpStatus.OK
        );
    }

    @GetMapping("/internal/email-notification")
    public ResponseEntity<Iterable<String>> getEmails(
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "30", required = false) Integer size
    )
    {
        return new ResponseEntity<>(
                userService.getEmailsForNotify(PageRequest.of(page, size)),
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id)
    {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}