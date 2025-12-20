package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.dto.response.*;
import com.javalab.taskservice.tables.records.TaskRecord;
import com.javalab.taskservice.util.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.javalab.taskservice.Tables.*;

@Repository
@RequiredArgsConstructor
public class TaskRepository
{
    private final DSLContext dsl;

    @Transactional(readOnly = true)
    public Result<TaskRecord> getAllTasks(Integer page, Integer size)
    {
        return dsl.selectFrom(TASK)
                .offset(page * size)
                .limit(size)
                .fetch();
    }

    @Transactional(readOnly = true)
    public TaskResponseDto getTask(Long id)
    {
        Result<Record> result = dsl.select(
                        TASK.asterisk(),
                        STARTER_CODE.ID_CODE,
                        STARTER_CODE.CODE,
                        STARTER_CODE.IS_DEFAULT,
                        CATEGORY.ID_CATEGORY,
                        CATEGORY.TITLE,
                        CATEGORY.DESCRIPTION,
                        CATEGORY.CREATED_AT
                )
                .from(TASK)
                .leftJoin(STARTER_CODE).on(TASK.ID_TASK.eq(STARTER_CODE.ID_TASK))
                .leftJoin(TASK_CATEGORY).on(TASK.ID_TASK.eq(TASK_CATEGORY.ID_TASK))
                .leftJoin(CATEGORY).on(TASK_CATEGORY.ID_CATEGORY.eq(CATEGORY.ID_CATEGORY))
                .where(TASK.ID_TASK.eq(id))
                .fetch();

        if (result.isEmpty())
            throw new TaskNotFoundException("Task with id=%d not found".formatted(id));

        Record firstRecord = result.get(0);

        List<CategoryResponseDto> categories = result.stream()
                .filter(record -> record.get(CATEGORY.ID_CATEGORY) != null)
                .map(record -> new CategoryResponseDto(
                        record.get(CATEGORY.TITLE),
                        record.get(CATEGORY.DESCRIPTION),
                        record.get(CATEGORY.CREATED_AT)
                ))
                .distinct()
                .toList();

        StarterCodeResponseDto starterCode = null;
        if (firstRecord.get(STARTER_CODE.ID_CODE) != null)
            starterCode = new StarterCodeResponseDto(
                    firstRecord.get(STARTER_CODE.CODE),
                    firstRecord.get(STARTER_CODE.IS_DEFAULT)
            );

        return new TaskResponseDto(
                firstRecord.get(TASK.ID_TASK),
                firstRecord.get(TASK.TITLE),
                firstRecord.get(TASK.DESCRIPTION),
                firstRecord.get(TASK.DIFFICULTY),
                firstRecord.get(TASK.CREATED_AT),
                firstRecord.get(TASK.UPDATED_AT),
                firstRecord.get(TASK.ID_AUTHOR),
                categories,
                starterCode
        );
    }

    @Transactional(readOnly = true)
    public TaskDetailedResponseDto getDetailedTask(Long id)
    {
        var task = getTask(id);

        var testCases = dsl.select(
                        TEST_CASE.ID_CODE,
                        TEST_CASE.INPUT,
                        TEST_CASE.EXPECTED_OUTPUT
                )
                .from(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(id)
                        .and(TEST_CASE.IS_HIDDEN.eq(false)))
                .fetchInto(TestCaseResponseDto.class);

        return new TaskDetailedResponseDto(task, testCases);
    }

    @Transactional
    public TaskRecord saveTask(TaskRequestDto taskDto)
    {
        return dsl.insertInto(TASK)
                .set(TASK.TITLE, taskDto.title())
                .set(TASK.DESCRIPTION, taskDto.description())
                .set(TASK.DIFFICULTY, taskDto.difficulty().name())
                .set(TASK.ID_AUTHOR, taskDto.idAuthor())
                .returning()
                .fetchOne();
    }

    @Transactional
    public Optional<TaskRecord> updateTask(Long id, TaskRequestDto taskDto)
    {
        return dsl.update(TASK)
                .set(TASK.TITLE, taskDto.title())
                .set(TASK.DESCRIPTION, taskDto.description())
                .set(TASK.DIFFICULTY, taskDto.difficulty().name())
                .set(TASK.UPDATED_AT, LocalDateTime.now())
                .where(TASK.ID_TASK.eq(id))
                .returning()
                .fetchOptional();
    }

    @Transactional
    public Optional<TaskRecord> deleteTask(Long id)
    {
        return dsl.deleteFrom(TASK)
                .where(TASK.ID_TASK.eq(id))
                .returning()
                .fetchOptional();
    }
}