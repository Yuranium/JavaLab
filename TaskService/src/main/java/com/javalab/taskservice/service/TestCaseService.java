package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.mapper.TestCaseMapper;
import com.javalab.taskservice.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TestCaseService
{
    private final TestCaseRepository testCaseRepository;

    private final TestCaseMapper testCaseMapper;

    public Collection<TestCaseResponseDto> createTestCaseForTask(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
        return testCaseMapper.toResponseDto(
                testCaseRepository.createTestCaseForTask(taskId, testCases)
        );
    }
}