package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.UserFilterDto;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<UserResponseDto>> getUsers(
            Pageable page, @ModelAttribute UserFilterDto filterDto
    )
    {
        return new ResponseEntity<>(
                userService.getUsers(page, filterDto),
                HttpStatus.OK
        );
    }

    @GetMapping("/internal/email-notification")
    public ResponseEntity<Page<String>> getEmails(Pageable page)
    {
        return new ResponseEntity<>(
                userService.getEmailsForNotify(page),
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