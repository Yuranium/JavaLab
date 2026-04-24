package com.javalab.executionservice.util;

import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.models.InMemoryJavaFileObject;
import com.javalab.executionservice.models.MemoryFileManager;
import com.javalab.executionservice.util.exception.CompilationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InMemoryCompiler
{
    private final ExecutionConfig executionConfig;

    public Class<?> compile(String className, String source)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileObject file = new InMemoryJavaFileObject(className, source);
        StandardJavaFileManager stdManager =
                compiler.getStandardFileManager(diagnostics, null, null);
        MemoryFileManager fileManager = new MemoryFileManager(stdManager);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                executionConfig.getCompiler().getParameters(),
                null,
                List.of(file)
        );
        boolean success = task.call();

        if (!success)
            throw new CompilationException(diagnostics.getDiagnostics().toString());

        byte[] bytes = fileManager.getClassBytes();
        MemoryClassLoader loader = new MemoryClassLoader();

        return loader.defineClass(className, bytes);
    }
}