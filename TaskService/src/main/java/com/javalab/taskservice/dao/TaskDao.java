package com.javalab.taskservice.dao;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.dto.response.*;
import com.javalab.taskservice.tables.records.TaskRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import static com.javalab.taskservice.Tables.*;
import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class TaskDao
{
    private final DSLContext dsl;

    @Transactional(readOnly = true)
    public Collection<TaskResponseDto> getAllTasks(Integer page, Integer size)
    {
        return dsl.select(
                        TASK.ID_TASK,
                        TASK.TITLE,
                        TASK.DIFFICULTY,
                        TASK.CREATED_AT,
                        TASK.UPDATED_AT,
                        TASK.ID_AUTHOR,
                        multiset(
                                select(
                                        CATEGORY.TITLE,
                                        CATEGORY.DESCRIPTION,
                                        CATEGORY.CREATED_AT
                                )
                                        .from(CATEGORY)
                                        .innerJoin(TASK_CATEGORY)
                                        .on(CATEGORY.ID_CATEGORY.eq(TASK_CATEGORY.ID_CATEGORY))
                                        .where(TASK_CATEGORY.ID_TASK.eq(TASK.ID_TASK))
                        )
                                .as("categories")
                                .convertFrom(r -> r.into(CategoryResponseDto.class))
                )
                .from(TASK)
                .orderBy(TASK.ID_TASK.asc())
                .offset(page * size)
                .limit(size)
                .fetchInto(TaskResponseDto.class);
    }

    @Transactional(readOnly = true)
    public Optional<TaskDetailedResponseDto> getDetailedTask(Long id, boolean loadHiddenTestCases)
    {
        return dsl.select(
                        TASK.ID_TASK,
                        TASK.TITLE,
                        TASK.DESCRIPTION,
                        TASK.DIFFICULTY,
                        TASK.CREATED_AT,
                        TASK.UPDATED_AT,
                        TASK.ID_AUTHOR,
                        multiset(
                                select(
                                        CATEGORY.TITLE,
                                        CATEGORY.DESCRIPTION,
                                        CATEGORY.CREATED_AT
                                )
                                        .from(CATEGORY)
                                        .innerJoin(TASK_CATEGORY)
                                        .on(CATEGORY.ID_CATEGORY.eq(TASK_CATEGORY.ID_CATEGORY))
                                        .where(TASK_CATEGORY.ID_TASK.eq(TASK.ID_TASK))
                        )
                                .as("categories")
                                .convertFrom(r -> r.into(CategoryResponseDto.class)),
                        row(
                                STARTER_CODE.ID_CODE,
                                STARTER_CODE.CODE,
                                STARTER_CODE.IS_DEFAULT)
                                .mapping(StarterCodeResponseDto::new),
                        multiset(
                                select(
                                        TEST_CASE.INPUT,
                                        TEST_CASE.EXPECTED_OUTPUT,
                                        TEST_CASE.IS_HIDDEN
                                )
                                        .from(TEST_CASE)
                                        .where(TEST_CASE.ID_TASK.eq(TASK.ID_TASK)
                                                .and(TEST_CASE.IS_HIDDEN.eq(loadHiddenTestCases)))
                                        .orderBy(TEST_CASE.ID_TASK.asc())
                        )
                                .as("test_cases")
                                .convertFrom(res -> res.into(TestCaseResponseDto.class))
                )
                .from(TASK)
                .innerJoin(STARTER_CODE)
                .on(TASK.ID_TASK.eq(STARTER_CODE.ID_TASK))
                .where(TASK.ID_TASK.eq(id))
                .fetchOptionalInto(TaskDetailedResponseDto.class);
    }

    @Transactional(readOnly = true)
    public Boolean existsById(Long id)
    {
        Integer count = dsl.selectCount().from(TASK)
                .where(TASK.ID_TASK.eq(id))
                .fetchOne(count());

        return count != null && count == 1;
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
                .set(TASK.UPDATED_AT, Instant.now())
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