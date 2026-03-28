package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.PublicUserResponseDto;
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

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<PublicUserResponseDto>> getUsers(
            Pageable page, @ModelAttribute UserFilterDto filterDto
    )
    {
        return new ResponseEntity<>(
                userService.getUsers(page, filterDto),
                HttpStatus.OK
        );
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserResponseDto> getUser(@PathVariable String username)
    {
        return new ResponseEntity<>(
                userService.getUser(username),
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

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @ModelAttribute UserRequestDto userDto
    )
    {
        return new ResponseEntity<>(
                userService.createUser(userDto),
                HttpStatus.CREATED
        );
    }
}