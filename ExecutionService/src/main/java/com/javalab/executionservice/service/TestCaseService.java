package com.javalab.executionservice.service;

import com.javalab.core.events.ExecutionAttemptEvent;
import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.dao.TestCaseDao;
import com.javalab.executionservice.models.dto.*;
import com.javalab.executionservice.models.enums.ExecutionStatus;
import com.javalab.executionservice.models.enums.TestCaseStatus;
import com.javalab.executionservice.service.kafka.KafkaProducer;
import com.javalab.executionservice.util.ExecutionStatusPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class TestCaseService
{
    private final ExecutionConfig executionConfig;

    private final TestCaseDao testCaseDao;

    private final ExecutionStatusPublisher publisher;

    private final KafkaProducer kafkaProducer;

    public void runTests(
            ExecutionRequestDto request,
            Class<?> clazz,
            Method method
    )
    {
        List<TestCaseDto> testCases = testCaseDao.getTestCases(request.taskId());

        if (testCases.isEmpty())
        {
            publisher.sendInfo(request.userId(), "Test-cases was not found");
            return;
        }
        List<TestExecutionResult> results = new ArrayList<>();
        boolean allPassed = true;
        publisher.sendInfo(request.userId(), "Start tests...");

        for (int i = 0; i < testCases.size(); i++)
        {
            TestExecutionResult result = executeTest(clazz, method, testCases.get(i), i + 1);

            results.add(result);
            allPassed &= result.isPassed();
            publisher.sendTestResult(request.userId(), result);
        }

        ExecutionResponseMessage finalResponse = new ExecutionResponseMessage(
                allPassed ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED,
                allPassed ? null : "Some tests failed",
                totalDuration(results),
                results
        );

        publisher.sendExecutionResult(request.userId(), finalResponse);
        kafkaProducer.sendExecutionAttemptEvent(
                new ExecutionAttemptEvent(
                        allPassed,
                        request.code(),
                        request.userId(),
                        request.taskId(),
                        Instant.now()
                )
        );
    }

    private Object invokeWithTimeout(Class<?> clazz, Method method, String input) throws Exception
    {
        try (ExecutorService executor = Executors.newSingleThreadExecutor())
        {
            Future<Object> future = executor.submit(() -> {
                Object instance = Modifier.isStatic(method.getModifiers())
                        ? null
                        : clazz.getDeclaredConstructor().newInstance();

                Object[] args = convert(input, method.getParameterTypes());
                return method.invoke(instance, args);
            });

            return future.get(
                    executionConfig.getTimeout().getExecution().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } catch (ExecutionException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex)
                throw ex;

            throw new RuntimeException(cause);
        }
    }

    private TestExecutionResult executeTest(
            Class<?> clazz,
            Method method,
            TestCaseDto tc,
            int testNumber
    )
    {
        long start = System.currentTimeMillis();
        try
        {
            Object output = invokeWithTimeout(clazz, method, tc.input());
            String actual = output.toString();
            String expected = tc.expectedOutput();
            boolean passed = actual.equals(expected);

            return new TestExecutionResult(
                    testNumber,
                    passed ? TestCaseStatus.PASSED : TestCaseStatus.FAILED,
                    actual,
                    expected,
                    null,
                    elapsed(start)
            );
        } catch (TimeoutException e)
        {
            return new TestExecutionResult(
                    testNumber,
                    TestCaseStatus.TIMEOUT,
                    null,
                    tc.expectedOutput(),
                    "Time limit exceeded",
                    elapsed(start)
            );
        } catch (IllegalArgumentException e)
        {
            return new TestExecutionResult(
                    testNumber,
                    TestCaseStatus.VALIDATION_ERROR,
                    null,
                    tc.expectedOutput(),
                    e.getMessage(),
                    elapsed(start)
            );
        }
        catch (Exception e)
        {
            return new TestExecutionResult(
                    testNumber,
                    TestCaseStatus.RUNTIME_ERROR,
                    null,
                    tc.expectedOutput(),
                    e.getMessage(),
                    elapsed(start)
            );
        }
    }

    private long elapsed(long start)
    {
        return System.currentTimeMillis() - start;
    }

    private long totalDuration(Collection<TestExecutionResult> results)
    {
        return results.stream()
                .mapToLong(TestExecutionResult::executionDuration)
                .sum();
    }

    private Object[] convert(String input, Class<?>[] types)
    {
        Object[] args = new Object[types.length];
        String[] inputs = input.split(",");
        if (inputs.length != types.length)
            throw new IllegalArgumentException(
                    "Expected %s parameters, but provided %s"
                            .formatted(inputs.length, types.length)
            );

        for (int i = 0; i < types.length; i++)
        {
            if (types[i] == String.class)
                args[i] = inputs[i].trim();

            if (types[i] == int.class || types[i] == Integer.class)
                args[i] = Integer.parseInt(inputs[i].trim());

            if (types[i] == long.class || types[i] == Long.class)
                args[i] = Long.parseLong(inputs[i].trim());

            if (types[i] == double.class || types[i] == Double.class)
                args[i] = Double.parseDouble(inputs[i].trim());
        }
        return args;
    }
}