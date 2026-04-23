package com.javalab.executionservice.service;

import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.enums.ExecutionStatus;
import com.javalab.executionservice.util.ExecutionStatusPublisher;
import com.javalab.executionservice.util.InMemoryCompiler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService
{
    private final ExecutionStatusPublisher publisher;

    private final InMemoryCompiler compiler;

    private final ExecutionConfig executionConfig;

    private final TestCaseService testCaseService;

    @Async("codeExecutionExecutor")
    public void execute(ExecutionRequestDto request)
    {
        publisher.sendInfo(request.userId(), "Compilation...");

        Class<?> clazz;
        try
        {
            clazz = compiler.compile(
                    executionConfig.getCompiler().getDefaultUserClassName(),
                    request.code()
            );
            Method solveMethod = findInputMethod(clazz);
            testCaseService.runTests(request, clazz, solveMethod);
        } catch (RuntimeException e)
        {
            publisher.sendInfo(request.userId(), e.getMessage());
            return;
        } catch (Exception e) // todo разграничить исключения (kafka и кастомные)
        {
            publisher.sendInfo(request.userId(), "Compilation error: " + e.getMessage());
            return;
        }
        publisher.sendInfo(request.userId(), "Start tests...");
    }

    private Method findInputMethod(Class<?> clazz)
    {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.getName().equals("solve"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Default method solve not found"));
    }
}