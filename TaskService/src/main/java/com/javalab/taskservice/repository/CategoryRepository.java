package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.CategoryRequestDto;
import com.javalab.taskservice.dto.response.CategoryResponseDto;
import com.javalab.taskservice.enums.JavaCategory;
import com.javalab.taskservice.tables.records.CategoryRecord;
import com.javalab.taskservice.tables.records.TaskCategoryRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

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

    @Transactional(readOnly = true)
    public Optional<CategoryResponseDto> getCategory(String title)
    {
        return dsl.select(
                        CATEGORY.TITLE,
                        CATEGORY.DESCRIPTION,
                        CATEGORY.CREATED_AT
                )
                .from(CATEGORY)
                .where(CATEGORY.TITLE.eq(title))
                .fetchOptionalInto(CategoryResponseDto.class);
    }

    @Transactional
    public Collection<CategoryResponseDto> saveCategoryForTask(Long taskId, Collection<JavaCategory> categories)
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
        return categoryRecords
                .map(category -> new CategoryResponseDto(
                        category.getTitle(),
                        category.getDescription(),
                        category.getCreatedAt()
                ));
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto category)
    {
        return dsl.insertInto(CATEGORY)
                .set(CATEGORY.TITLE, category.title())
                .set(CATEGORY.DESCRIPTION, category.description())
                .returningResult(
                        CATEGORY.TITLE,
                        CATEGORY.DESCRIPTION,
                        CATEGORY.CREATED_AT
                )
                .fetchOneInto(CategoryResponseDto.class);
    }

    @Transactional
    public Optional<CategoryResponseDto> updateCategory(String title, CategoryRequestDto categoryDto)
    {
        return dsl.update(CATEGORY)
                .set(CATEGORY.TITLE, categoryDto.title())
                .set(CATEGORY.DESCRIPTION, categoryDto.description())
                .where(CATEGORY.TITLE.eq(title))
                .returningResult(
                        CATEGORY.TITLE,
                        CATEGORY.DESCRIPTION,
                        CATEGORY.CREATED_AT
                )
                .fetchOptionalInto(CategoryResponseDto.class);
    }

    @Transactional
    public Optional<CategoryRecord> deleteCategory(String title)
    {
        return dsl.deleteFrom(CATEGORY)
                .where(CATEGORY.TITLE.eq(title))
                .returning()
                .fetchOptional();
    }
}