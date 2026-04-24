package com.javalab.executionservice.models;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager>
{
    private ByteArrayJavaFileObject classFileObject;

    public MemoryFileManager(JavaFileManager fileManager)
    {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling
    )
    {
        classFileObject = new ByteArrayJavaFileObject(className, kind);
        return classFileObject;
    }

    public byte[] getClassBytes()
    {
        return classFileObject.getBytes();
    }
}