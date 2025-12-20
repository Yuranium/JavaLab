package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.TestCaseRequestDto;
import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.tables.records.TestCaseRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

import static com.javalab.taskservice.Tables.TEST_CASE;

@Repository
@RequiredArgsConstructor
public class TestCaseRepository
{
    private final DSLContext dsl;

    @Transactional
    public TestCaseResponseDto createTestCase(Long taskId, TestCaseRequestDto testCaseDto)
    {
        return dsl.insertInto(TEST_CASE)
                .set(TEST_CASE.INPUT, testCaseDto.input())
                .set(TEST_CASE.EXPECTED_OUTPUT, testCaseDto.expectedOutput())
                .set(TEST_CASE.IS_HIDDEN, testCaseDto.isHidden())
                .set(TEST_CASE.ID_TASK, taskId)
                .returningResult(
                        TEST_CASE.ID_CASE,
                        TEST_CASE.INPUT,
                        TEST_CASE.EXPECTED_OUTPUT
                )
                .fetchOneInto(TestCaseResponseDto.class);
    }

    @Transactional
    public Collection<TestCaseRecord> createTestCasesForTask(
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

    @Transactional
    public Optional<TestCaseResponseDto> updateTestCase(Long testCaseId, TestCaseRequestDto testCaseDto)
    {
        return dsl.update(TEST_CASE)
                .set(TEST_CASE.INPUT, testCaseDto.input())
                .set(TEST_CASE.EXPECTED_OUTPUT, testCaseDto.expectedOutput())
                .set(TEST_CASE.IS_HIDDEN, testCaseDto.isHidden())
                .where(TEST_CASE.ID_CASE.eq(testCaseId))
                .returningResult(
                        TEST_CASE.ID_CASE,
                        TEST_CASE.INPUT,
                        TEST_CASE.EXPECTED_OUTPUT
                )
                .fetchOptionalInto(TestCaseResponseDto.class);
    }

    @Transactional
    public Optional<TestCaseRecord> deleteTestCase(Long testCaseId)
    {
        return dsl.deleteFrom(TEST_CASE)
                .where(TEST_CASE.ID_CASE.eq(testCaseId))
                .returning()
                .fetchOptional();
    }
}