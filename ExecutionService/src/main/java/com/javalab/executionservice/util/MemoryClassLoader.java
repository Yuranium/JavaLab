package com.javalab.executionservice.util;

public class MemoryClassLoader extends ClassLoader
{
    public Class<?> defineClass(String name, byte[] bytes)
    {
        return defineClass(name, bytes, 0, bytes.length);
    }
}