package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.TaskRequestDto;
import com.javalab.taskservice.tables.records.TaskRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.javalab.taskservice.Tables.TASK;

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
    public Optional<TaskRecord> getTask(Long id)
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
                .set(TASK.DIFFICULTY, taskDto.difficulty())
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
                .set(TASK.DIFFICULTY, taskDto.difficulty())
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