package com.javalab.executionservice.models;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class InMemoryJavaFileObject extends SimpleJavaFileObject
{
    private final String source;

    public InMemoryJavaFileObject(String className, String source)
    {
        super(URI.create("string:///" + className + ".java"), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return source;
    }
}