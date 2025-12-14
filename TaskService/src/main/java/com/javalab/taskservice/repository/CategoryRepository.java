package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.javalab.taskservice.Tables.CATEGORY;

@Repository
@RequiredArgsConstructor
public class CategoryRepository
{
    private final DSLContext dsl;

    @Transactional(readOnly = true)
    public Iterable<CategoryResponseDto> getCategories(Integer page, Integer size)
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
}