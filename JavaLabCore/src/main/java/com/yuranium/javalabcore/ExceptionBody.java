package com.yuranium.javalabcore;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ExceptionBody implements Serializable
{
    private final Integer status;

    private final LocalDateTime timestamp;

    private final String message;

    public ExceptionBody(Integer status, String message)
    {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public Integer getStatus()
    {
        return status;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }
}