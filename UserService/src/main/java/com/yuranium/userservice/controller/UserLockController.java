package com.yuranium.userservice.controller;

import com.yuranium.userservice.models.dto.userlock.UserUnlockRequest;
import com.yuranium.userservice.models.dto.userlock.UserLockRequest;
import com.yuranium.userservice.service.UserLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/user/access")
@RequiredArgsConstructor
public class UserLockController
{
    private final UserLockService userLockService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{id}/lock")
    public void lockUser(
            @PathVariable Long id,
            @RequestBody UserLockRequest duration
    )
    {
        userLockService.lockUser(id, duration);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{id}/unlock")
    public void unlockUser(
            @PathVariable Long id,
            @RequestBody UserUnlockRequest unlockTime
    )
    {
        userLockService.unlockUser(id, unlockTime);
    }
}