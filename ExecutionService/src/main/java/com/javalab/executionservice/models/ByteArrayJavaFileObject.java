package com.javalab.executionservice.models;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class ByteArrayJavaFileObject extends SimpleJavaFileObject
{
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public ByteArrayJavaFileObject(String name, Kind kind)
    {
        super(URI.create("bytes:///" + name), kind);
    }

    @Override
    public OutputStream openOutputStream()
    {
        return outputStream;
    }

    public byte[] getBytes()
    {
        return outputStream.toByteArray();
    }
}