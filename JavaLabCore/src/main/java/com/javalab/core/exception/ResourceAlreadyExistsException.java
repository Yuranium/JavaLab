package com.javalab.core.exception;

public class ResourceAlreadyExistsException extends RuntimeException
{
    public ResourceAlreadyExistsException(String message)
    {
        super(message);
    }
}