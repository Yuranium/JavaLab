package com.javalab.taskservice.dao;

import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.tables.records.TestCaseRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static com.javalab.taskservice.Tables.TEST_CASE;

@Repository
@RequiredArgsConstructor
public class TestCaseDao
{
    private final DSLContext dsl;

    @Transactional
    public Collection<TestCaseResponseDto> createTestCases(
            Long taskId, Collection<TestCaseRequestDto> testCases
    )
    {
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
        return preparedTestCases.stream()
                .map(ts -> new TestCaseResponseDto(
                        ts.getInput(),
                        ts.getExpectedOutput(),
                        ts.getIsHidden()
                ))
                .toList();
    }

    @Transactional
    public Collection<TestCaseResponseDto> updateTestCases(
            Long taskId, Collection<TestCaseRequestDto> testCaseDto
    )
    {
        deleteTestCases(taskId);
        return createTestCases(taskId, testCaseDto);
    }

    @Transactional
    public void deleteTestCases(Long taskId)
    {
        dsl.delete(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(taskId))
                .execute();
    }
}