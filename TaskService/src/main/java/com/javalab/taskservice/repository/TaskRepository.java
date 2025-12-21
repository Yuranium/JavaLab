package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.dto.response.*;
import com.javalab.taskservice.tables.records.CategoryRecord;
import com.javalab.taskservice.tables.records.StarterCodeRecord;
import com.javalab.taskservice.tables.records.TaskRecord;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.javalab.taskservice.Tables.*;

@Repository
@RequiredArgsConstructor
public class TaskRepository
{
    private final DSLContext dsl;

    @Transactional(readOnly = true)
    public Collection<TaskResponseDto> getAllTasks(Integer page, Integer size)
    {
        List<TaskRecord> tasks = dsl.selectFrom(TASK)
                .offset(page * size)
                .limit(size)
                .fetchInto(TaskRecord.class);

        if (tasks.isEmpty())
            return Collections.emptyList();

        List<Long> taskIds = tasks.stream()
                .map(TaskRecord::getIdTask)
                .toList();

        Map<Long, List<CategoryRecord>> categoriesByTaskId = dsl.select()
                .from(CATEGORY)
                .join(TASK_CATEGORY).on(CATEGORY.ID_CATEGORY.eq(TASK_CATEGORY.ID_CATEGORY))
                .where(TASK_CATEGORY.ID_TASK.in(taskIds))
                .fetch()
                .intoGroups(
                        record -> record.get(TASK_CATEGORY.ID_TASK),
                        record -> record.into(CategoryRecord.class)
                );

        Map<Long, StarterCodeRecord> starterCodesByTaskId = dsl.selectFrom(STARTER_CODE)
                .where(STARTER_CODE.ID_TASK.in(taskIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        StarterCodeRecord::getIdTask,
                        Function.identity()
                ));

        return tasks.stream()
                .map(task -> mapToTaskResponseDto(
                        task,
                        categoriesByTaskId.getOrDefault(task.getIdTask(), Collections.emptyList()),
                        starterCodesByTaskId.get(task.getIdTask())
                ))
                .toList();
    }

    private TaskResponseDto mapToTaskResponseDto(
            TaskRecord task, List<CategoryRecord> categories, StarterCodeRecord starterCode
    )
    {
        return new TaskResponseDto(
                task.getIdTask(),
                task.getTitle(),
                task.getDescription(),
                task.getDifficulty(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getIdAuthor(),
                categories.stream()
                        .map(category -> new CategoryResponseDto(
                                category.getTitle(),
                                category.getDescription(),
                                category.getCreatedAt()
                        ))
                        .toList(),
                starterCode != null ?
                        new StarterCodeResponseDto(
                                starterCode.getIdCode(),
                                starterCode.getCode(),
                                starterCode.getIsDefault()
                        ) : null
        );
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
            throw new ResourceNotFoundException("The task with id=%d not found".formatted(id));

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
                    firstRecord.get(STARTER_CODE.ID_CODE),
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
                        TEST_CASE.ID_CASE,
                        TEST_CASE.INPUT,
                        TEST_CASE.EXPECTED_OUTPUT
                )
                .from(TEST_CASE)
                .where(TEST_CASE.ID_TASK.eq(id)
                        .and(TEST_CASE.IS_HIDDEN.eq(false)))
                .fetchInto(TestCaseResponseDto.class);

        return new TaskDetailedResponseDto(task, testCases);
    }

    @Transactional(readOnly = true)
    public Optional<TaskRecord> getOnlyTask(Long id)
    {
        return dsl.selectFrom(TASK)
                .where(TASK.ID_TASK.eq(id))
                .fetchOptional();
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
    public Optional<TaskUpdatedResponseDto> updateTask(Long id, TaskRequestDto taskDto)
    {
        return dsl.update(TASK)
                .set(TASK.TITLE, taskDto.title())
                .set(TASK.DESCRIPTION, taskDto.description())
                .set(TASK.DIFFICULTY, taskDto.difficulty().name())
                .set(TASK.UPDATED_AT, LocalDateTime.now())
                .where(TASK.ID_TASK.eq(id))
                .returning()
                .fetchOptionalInto(TaskUpdatedResponseDto.class);
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