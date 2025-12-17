package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.tables.records.TestCaseRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class TestCaseRepository
{
    private final DSLContext dsl;

    @Transactional
    public Collection<TestCaseRecord> createTestCaseForTask(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        var preparedTestCases = testCases.stream()
                .map(ts -> {
                    var testCase = new TestCaseRecord();
                    testCase.setInput(ts.input());
                    testCase.setExpectedOutput(ts.expectedOutput());
                    testCase.setIsHidden(ts.isHidden());
                    testCase.setIdTask(taskId);
                    return testCase;
                })
                .toList();

        dsl.batchInsert(preparedTestCases).execute();
        return preparedTestCases;
    }
}