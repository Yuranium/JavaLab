package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.response.CategoryResponseDto;
import com.javalab.taskservice.tables.records.CategoryRecord;
import com.javalab.taskservice.tables.records.TaskCategoryRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static com.javalab.taskservice.Tables.CATEGORY;

@Repository
@RequiredArgsConstructor
public class CategoryRepository
{
    private final DSLContext dsl;

    @Transactional(readOnly = true)
    public Collection<CategoryResponseDto> getCategories(Integer page, Integer size)
    {
        return dsl.select(
                        CATEGORY.TITLE,
                        CATEGORY.DESCRIPTION,
                        CATEGORY.CREATED_AT
                )
                .from(CATEGORY)
                .offset(page * size)
                .limit(size)
                .fetchInto(CategoryResponseDto.class);
    }

    @Transactional
    public Collection<CategoryRecord> saveCategories(Long taskId, Collection<String> categories)
    {
        var categoryRecords = dsl.selectFrom(CATEGORY)
                .where(CATEGORY.TITLE.in(categories))
                .fetch();

        var taskCategories = categoryRecords.stream()
                .map(category -> {
                    var taskCategory = new TaskCategoryRecord();
                    taskCategory.setIdTask(taskId);
                    taskCategory.setIdCategory(category.getIdCategory());
                    return taskCategory;
                })
                .toList();

        dsl.batchInsert(taskCategories).execute();
        return categoryRecords;
    }
}