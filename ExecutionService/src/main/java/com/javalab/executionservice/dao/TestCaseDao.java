package com.javalab.executionservice.dao;

import com.javalab.core.events.TestCasePayload;
import com.javalab.executionservice.models.dto.TestCaseDto;
import com.javalab.executionservice.jooq.tables.records.TestCaseRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static com.javalab.executionservice.jooq.tables.TestCase.TEST_CASE;

@Repository
@RequiredArgsConstructor
public class TestCaseDao
{
    private final DSLContext dsl;

    @Transactional
    public void saveAllTestCases(Long taskId, Collection<TestCasePayload> testCases)
    {
        dsl.batchInsert(preparedTestCases(taskId, testCases))
                .execute();
    }

    @Transactional
    public void updateAllTestCases(Long taskId, Collection<TestCasePayload> testCases)
    {
        dsl.batchUpdate(preparedTestCases(taskId, testCases))
                .execute();
    }

    @Transactional
    public void deleteAllTestCases(Long taskId)
    {
        dsl.deleteFrom(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(taskId))
                .execute();
    }

    @Transactional(readOnly = true)
    public List<TestCaseDto> getTestCases(Long taskId)
    {
        return dsl.select(
                        TEST_CASE.INPUT,
                        TEST_CASE.EXPECTED_OUTPUT,
                        TEST_CASE.IS_HIDDEN
                )
                .from(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(taskId))
                .orderBy(TEST_CASE.ID_TASK.asc())
                .fetchInto(TestCaseDto.class);
    }

    private Collection<TestCaseRecord> preparedTestCases(
            Long taskId,
            Collection<TestCasePayload> testCases
    )
    {
        return testCases.stream()
                .map(testCase -> {
                    var record = new TestCaseRecord();
                    record.setInput(testCase.input());
                    record.setExpectedOutput(testCase.expectedOutput());
                    record.setIsHidden(testCase.isHidden());
                    record.setIdTask(taskId);
                    return record;
                })
                .toList();
    }
}