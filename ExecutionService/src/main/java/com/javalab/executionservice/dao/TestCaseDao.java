package com.javalab.executionservice.dao;

import com.javalab.core.events.TestCasePayload;
import com.javalab.executionservice.models.dto.TestCaseDto;
import com.javalab.executionservice.tables.records.TestCaseRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.javalab.executionservice.tables.TestCase.TEST_CASE;

@Repository
@RequiredArgsConstructor
public class TestCaseDao
{
    private final DSLContext dsl;

    public void saveAllTestCases(Long taskId, Collection<TestCasePayload> testCases)
    {
        dsl.batchInsert(
                testCases.stream()
                .map(testCase -> {
                    var record = new TestCaseRecord();
                    record.setInput(testCase.input());
                    record.setExpectedOutput(testCase.expectedOutput());
                    record.setIdTask(taskId);
                    return record;
                })
                .toList())
                .execute();
    }

    public void updateAllTestCases(Long taskId, Collection<TestCasePayload> testCases)
    {
        dsl.batchUpdate(
                testCases.stream()
                        .map(testCase -> {
                            var record = new TestCaseRecord();
                            record.setInput(testCase.input());
                            record.setExpectedOutput(testCase.expectedOutput());
                            record.setIdTask(taskId);
                            return record;
                        })
                        .toList())
                .execute();
    }

    public void deleteAllTestCases(Long taskId)
    {
        dsl.deleteFrom(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(taskId))
                .execute();
    }

    public List<TestCaseDto> getTestCases(Long taskId)
    {
        return dsl.select(TEST_CASE.INPUT, TEST_CASE.EXPECTED_OUTPUT)
                .from(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(taskId))
                .fetchInto(TestCaseDto.class);
    }
}