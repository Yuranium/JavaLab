package com.javalab.taskservice.service;

import com.javalab.core.events.TestCaseEvent;
import com.javalab.core.events.TestCaseEventType;
import com.javalab.core.events.TestCasePayload;
import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.dao.TestCaseDao;
import com.javalab.taskservice.service.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TestCaseService
{
    private final TestCaseDao testCaseDao;

    private final KafkaSender kafkaSender;

    @Transactional
    public Collection<TestCaseResponseDto> createTestCases(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        Collection<TestCaseResponseDto> savedCases = testCaseDao
                .createTestCases(taskId, testCases);

        sendEvent(taskId, savedCases, TestCaseEventType.TEST_CASE_CREATED);
        return savedCases;
    }

    @Transactional
    public Collection<TestCaseResponseDto> updateTestCases(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
        var responseDto = testCaseDao.updateTestCases(taskId, testCases);

        sendEvent(taskId, responseDto, TestCaseEventType.TEST_CASE_UPDATED);
        return responseDto;
    }

    @Transactional
    public void deleteTestCases(Long taskId)
    {
        testCaseDao.deleteTestCases(taskId);
        sendEvent(taskId, null, TestCaseEventType.TEST_CASE_DELETED);
    }

    private void sendEvent(
            Long taskId,
            Collection<TestCaseResponseDto> testCase,
            TestCaseEventType type
    )
    {
        kafkaSender.sendTestCaseEvent(new TestCaseEvent(
                type,
                taskId,
                testCase == null
                        ? null
                        : testCase.stream()
                        .map(e -> new TestCasePayload(
                                e.input(),
                                e.expectedOutput())
                        )
                        .toList())
        );
    }
}