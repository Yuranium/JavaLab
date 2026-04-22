package com.javalab.executionservice.service;

import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.dto.TestCaseDto;
import com.javalab.executionservice.models.dto.TestCaseResult;
import com.javalab.executionservice.models.enums.ExecutionStatus;
import com.javalab.executionservice.util.ExecutionStatusPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class TestCaseService
{
    private final ExecutionConfig executionConfig;

    private final ExecutionStatusPublisher publisher;

    public void runTests(
            ExecutionRequestDto request,
            List<TestCaseDto> testCases,
            Class<?> clazz,
            Method solveMethod
    )
    {
        List<TestCaseResult> results = new ArrayList<>();
        boolean allPassed = true;

        for (int i = 0; i < testCases.size(); i++)
        {
            TestCaseDto tc = testCases.get(i);

            try
            {
                Object output = runTest(clazz, solveMethod, tc.input());
                String actual = output.toString();
                String expected = tc.expectedOutput();
                boolean passed = actual.equals(expected);
                allPassed &= passed;

                results.add(new TestCaseResult(i + 1, passed, actual, testCases.get(i).expectedOutput()));
            } catch (TimeoutException e)
            {
                results.add(new TestCaseResult(i + 1, false, "TIMEOUT", testCases.get(i).expectedOutput()));
                publisher.publish(request.taskId(),
                        ExecutionStatus.TIMEOUT,
                        "Timeout during run test %s".formatted(i + 1),
                        results);
                return;

            } catch (Exception e)
            {
                allPassed = false;
                results.add(new TestCaseResult(
                        i + 1, false,
                        "RUNTIME ERROR: " + e.getMessage(),
                        testCases.get(i).expectedOutput())
                );
            }

            publisher.publish(request.taskId(),
                    ExecutionStatus.PROCESSING,
                    "Tests (" + (i + 1) + "/" + testCases.size() + ")",
                    results);
        }

        publisher.publish(request.taskId(),
                allPassed ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED,
                allPassed ? "OK" : "There are errors in tests",
                results);
    }

    private Object runTest(Class<?> clazz, Method method, String input) throws Exception
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