package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.CategoryResponseDto;
import com.javalab.taskservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService
{
    private final CategoryRepository categoryRepository;

    public Iterable<CategoryResponseDto> getCategories(Integer page, Integer size)
    {
        return categoryRepository.getCategories(page, size);
    }
}