package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.TaskRequestDto;
import com.javalab.taskservice.tables.records.TaskRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}