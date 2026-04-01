package com.javalab.taskservice.service;

import com.javalab.core.events.TestCaseEvent;
import com.javalab.core.events.TestCaseEventType;
import com.javalab.core.events.TestCasePayload;
import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.repository.TaskRepository;
import com.javalab.taskservice.repository.TestCaseRepository;
import com.javalab.taskservice.service.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TestCaseService
{
    private final TestCaseRepository testCaseRepository;

    private final TaskRepository taskRepository;

    private final KafkaSender kafkaSender;

    public TestCaseResponseDto createTestCase(
            Long taskId, TestCaseRequestDto testCaseDto
    )
    {
        if (!taskRepository.existsById(taskId))
            throw new ResourceNotFoundException(
                    "The task with id=%d not found".formatted(taskId)
            );

        TestCaseResponseDto testCase = testCaseRepository.createTestCase(taskId, testCaseDto);
        sendEvent(taskId, testCase, TestCaseEventType.TEST_CASE_CREATED);
        return testCase;
    }

    public Collection<TestCaseResponseDto> createTestCasesForTask(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        Collection<TestCaseResponseDto> cases = testCaseRepository.createTestCasesForTask(taskId, testCases);
        cases.forEach(testCase -> sendEvent(
                taskId,
                testCase,
                TestCaseEventType.TEST_CASE_CREATED
        ));
        return cases;
    }

    public TestCaseResponseDto updateTestCase(
            Long taskId, Long testCaseId, TestCaseRequestDto testCaseDto
    )
    {
        TestCaseResponseDto responseDto = testCaseRepository.updateTestCase(taskId, testCaseId, testCaseDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The test-case with id=%d not found".formatted(testCaseId)
                        )
                );
        sendEvent(taskId, responseDto, TestCaseEventType.TEST_CASE_UPDATED);
        return responseDto;
    }

    public void deleteTestCase(Long taskId, Long testCaseId)
    {
        TestCaseResponseDto responseDto = testCaseRepository.deleteTestCase(taskId, testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The test-case with id=%d not found".formatted(testCaseId)
                        )
                );
        sendEvent(taskId, responseDto, TestCaseEventType.TEST_CASE_DELETED);
    }

    private void sendEvent(Long taskId, TestCaseResponseDto testCase, TestCaseEventType type)
    {
        kafkaSender.sendTestCaseEvent(new TestCaseEvent(
                type,
                taskId,
                new TestCasePayload(
                        testCase.id(),
                        testCase.input(),
                        testCase.expectedOutput())
        ));
    }
}