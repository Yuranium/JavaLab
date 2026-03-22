package com.yuranium.userservice.models.dto;

import java.io.Serializable;

public record UserFilterDto(
        Boolean activity,

        Boolean notifyEnabled

) implements Serializable {}