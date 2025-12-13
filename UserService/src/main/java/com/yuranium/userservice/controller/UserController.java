package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserUpdateDto;
import com.yuranium.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id)
    {
        return new ResponseEntity<>(
                userService.getUser(id),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@ModelAttribute UserRequestDto userDto)
    {
        return new ResponseEntity<>(
                userService.createUser(userDto),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @ModelAttribute UserUpdateDto userDto
    )
    {
        return new ResponseEntity<>(
                userService.updateUser(id, userDto),
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