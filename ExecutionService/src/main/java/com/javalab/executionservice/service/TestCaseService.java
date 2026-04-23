package com.javalab.executionservice.service;

import com.javalab.core.events.ExecutionAttemptEvent;
import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.models.dao.TestCaseDao;
import com.javalab.executionservice.models.dto.ExecutionRequestDto;
import com.javalab.executionservice.models.dto.TestCaseDto;
import com.javalab.executionservice.models.dto.TestCaseResult;
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
            Method solveMethod
    ) {
        List<TestCaseDto> testCases = testCaseDao.getTestCases(request.taskId());

        if (testCases.isEmpty())
        {
            publisher.publishMessage(request.userId(),
                    ExecutionStatus.FAILED,
                    "Test-cases was not found"
            );
            return;
        }
        List<TestCaseResult> results = new ArrayList<>();
        boolean allPassed = true;

        for (int i = 0; i < testCases.size(); i++)
        {
            TestCaseDto tc = testCases.get(i);
            try
            {
                TestCaseResult result = runTest(clazz, solveMethod, tc, i);
                results.add(result);

                allPassed &= result.passed();

            } catch (TimeoutException e)
            {
                handleTimeout(request, results, i);
                return;
            } catch (Exception e)
            {
                allPassed = false;
                results.add(handleRuntimeError(i, tc, e));
            }
            publishProgress(request, results, i, testCases.size());
        }
        finalizeExecution(request, results, allPassed);
    }

    private TestCaseResult runTest(Class<?> clazz, Method method, TestCaseDto dto, int index) throws Exception
    {
        try (ExecutorService executor = Executors.newSingleThreadExecutor())
        {
            long startTime = System.currentTimeMillis(), endTime;
            Future<Object> future = executor.submit(() -> {

                Object instance = Modifier.isStatic(method.getModifiers())
                        ? null
                        : clazz.getDeclaredConstructor().newInstance();

                Object[] args = convert(dto.input(), method.getParameterTypes());
                return method.invoke(instance, args);
            });

            Object output = future.get(
                    executionConfig.getTimeout().getExecution().toMillis(),
                    TimeUnit.MILLISECONDS
            );
            endTime = System.currentTimeMillis();
            String actual = output == null ? "" : output.toString();
            String expected = dto.expectedOutput();
            boolean passed = actual.equals(expected);
            return new TestCaseResult(
                    index,
                    passed,
                    passed ? TestCaseStatus.PASSED : TestCaseStatus.FAILED,
                    actual,
                    expected,
                    "", // todo
                    endTime - startTime
            );
        } catch (Exception e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex)
                throw ex;

            throw new RuntimeException(cause);
        }
    }

    private void finalizeExecution(
            ExecutionRequestDto request,
            List<TestCaseResult> results,
            boolean allPassed
    ) {

        publisher.publish(
                request.taskId(),
                allPassed ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED,
                allPassed ? "OK" : "There are errors in tests",
                results
        );

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