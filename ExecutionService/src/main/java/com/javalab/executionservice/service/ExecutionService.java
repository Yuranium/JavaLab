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
        publisher.publishMessage(request.userId(),
                ExecutionStatus.PROCESSING,
                "Compilation...");

        Class<?> clazz;
        try
        {
            clazz = compiler.compile(
                    executionConfig.getCompiler().getDefaultUserClassName(),
                    request.code()
            );
        } catch (Exception e)
        {
            publisher.publishMessage(request.userId(),
                    ExecutionStatus.FAILED,
                    "Compilation error: " + e.getMessage());
            return;
        }

        Method solveMethod = findInputMethod(clazz);
        publisher.publishMessage(
                request.userId(),
                ExecutionStatus.PROCESSING,
                "Start tests..."
        );

        testCaseService.runTests(request, clazz, solveMethod);
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