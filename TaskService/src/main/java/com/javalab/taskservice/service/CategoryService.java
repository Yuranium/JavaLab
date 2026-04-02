package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.CategoryRequestDto;
import com.javalab.taskservice.dto.response.CategoryResponseDto;
import com.javalab.taskservice.enums.JavaCategory;
import com.javalab.taskservice.repository.CategoryRepository;
import com.javalab.core.exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CategoryService
{
    private final CategoryRepository categoryRepository;

    public Collection<CategoryResponseDto> getCategories(Integer page, Integer size)
    {
        return categoryRepository.getCategories(page, size);
    }

    public CategoryResponseDto getCategory(String title)
    {
        return categoryRepository.getCategory(title)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The category with title=%s not found".formatted(title)
                        )
                );
    }

    public Collection<CategoryResponseDto> saveCategoryForTask(
            Long taskId, Collection<JavaCategory> categories
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        return categoryRepository.saveCategoryForTask(taskId, categories);
    }

    public Collection<CategoryResponseDto> updateCategoryForTask(
            Long taskId, Collection<JavaCategory> categories
    )
    {
        if (taskId == null)
            throw new NullPointerException("taskId is null");

        return categoryRepository.updateCategoryForTask(taskId, categories);
    }

    public CategoryResponseDto createCategory(CategoryRequestDto categoryDto)
    {
        if (!categoryDto.title().startsWith("JAVA_"))
            throw new IllegalArgumentException(
                    "Invalid category name. Category name must start with 'JAVA_'"
            );

        if (categoryRepository.getCategory(categoryDto.title()).isPresent())
            throw new ResourceAlreadyExistsException(
                    "The category with title=%s already exists"
                            .formatted(categoryDto.title())
            );

        return categoryRepository.createCategory(categoryDto);
    }

    public CategoryResponseDto updateCategory(
            String title, CategoryRequestDto categoryDto
    )
    {
        if (!categoryDto.title().startsWith("JAVA_"))
            throw new IllegalArgumentException(
                    "Invalid category name. Category name must start with 'JAVA_'"
            );

        if (categoryRepository.getCategory(categoryDto.title()).isPresent())
            throw new ResourceAlreadyExistsException(
                    "The category with title=%s already exists".formatted(title)
            );

        return categoryRepository.updateCategory(title, categoryDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The category with title=%s not found".formatted(title)
                        )
                );
    }

    public void deleteCategory(String title)
    {
        categoryRepository.deleteCategory(title)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "The category with title=%s not found".formatted(title)
                        )
                );
    }
}