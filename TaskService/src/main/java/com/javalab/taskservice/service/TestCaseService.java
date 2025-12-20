package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.mapper.TestCaseMapper;
import com.javalab.taskservice.repository.TaskRepository;
import com.javalab.taskservice.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TestCaseService
{
    private final TestCaseRepository testCaseRepository;

    private final TestCaseMapper testCaseMapper;

    private final TaskRepository taskRepository;

    public TestCaseResponseDto createTestCaseForTask(
            Long taskId, TestCaseRequestDto testCaseDto
    )
    {
        if (taskRepository.getOnlyTask(taskId).isPresent())
            return testCaseRepository.createTestCase(taskId, testCaseDto);

        throw new ResourceNotFoundException(
                "The task with id=%d not found".formatted(taskId)
        );
    }

    public Collection<TestCaseResponseDto> createTestCasesForTask(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
        return testCaseMapper.toResponseDto(
                testCaseRepository.createTestCasesForTask(taskId, testCases)
        );
    }

    public TestCaseResponseDto updateTestCase(
            Long testCaseId, TestCaseRequestDto testCaseDto
    )
    {
        return testCaseRepository.updateTestCase(testCaseId, testCaseDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The test-case with id=%d not found".formatted(testCaseId)
                        )
                );
    }

    public void deleteTestCase(Long testCaseId)
    {
        testCaseRepository.deleteTestCase(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The test-case with id=%d not found".formatted(testCaseId)
                        )
                );
    }
}