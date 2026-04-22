package com.javalab.executionservice.service;

import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.models.dao.TestCaseDao;
import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.dto.TestCaseDto;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService
{
    private final TestCaseDao testCaseDao;

    private final ExecutionStatusPublisher publisher;

    private final InMemoryCompiler compiler;

    private final ExecutionConfig executionConfig;

    private final TestCaseService testCaseService;

    @Async("codeExecutionExecutor")
    public void execute(ExecutionRequestDto request)
    {
        List<TestCaseDto> testCases = testCaseDao.getTestCases(request.taskId());

        if (testCases.isEmpty())
        {
            publisher.publish(request.taskId(),
                    ExecutionStatus.FAILED,
                    "Test-cases was not found",
                    null);
            return;
        }

        publisher.publish(request.taskId(),
                ExecutionStatus.PROCESSING,
                "Compilation...",
                null);

        Class<?> clazz;
        try
        {
            clazz = compiler.compile(
                    executionConfig.getCompiler().getDefaultUserClassName(),
                    request.code()
            );
        } catch (Exception e)
        {
            publisher.publish(request.taskId(),
                    ExecutionStatus.FAILED,
                    "Compilation error: " + e.getMessage(),
                    null);
            return;
        }

        Method solveMethod = findInputMethod(clazz);
        publisher.publish(
                request.taskId(),
                ExecutionStatus.PROCESSING,
                "Start tests...",
                null
        );

        testCaseService.runTests(request, testCases, clazz, solveMethod);
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