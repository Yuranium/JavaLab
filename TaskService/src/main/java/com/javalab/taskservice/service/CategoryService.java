package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.CategoryRequestDto;
import com.javalab.taskservice.dto.response.CategoryResponseDto;
import com.javalab.taskservice.enums.JavaCategory;
import com.javalab.taskservice.mapper.CategoryMapper;
import com.javalab.taskservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CategoryService
{
    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public Collection<CategoryResponseDto> getCategories(Integer page, Integer size)
    {
        return categoryRepository.getCategories(page, size);
    }

    public Collection<CategoryResponseDto> saveCategoryForTask(
            Long taskId, Collection<JavaCategory> categories
    )
    {
        return categoryMapper.toResponseDto(
                categoryRepository.saveCategories(taskId, categories)
        );
    }

    public CategoryResponseDto createCategory(CategoryRequestDto categoryName)
    {
        if (!categoryName.title().startsWith("JAVA_"))
            throw new IllegalArgumentException(
                    "Invalid category name. Category name must start with 'JAVA_'"
            );

        return categoryRepository.createCategory(categoryName);
    }
}