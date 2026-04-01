package com.javalab.taskservice.controller;

import com.javalab.taskservice.dto.request.CategoryRequestDto;
import com.javalab.taskservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/task/category")
public class CategoryController
{
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getCategories(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "30") Integer size
    )
    {
        return new ResponseEntity<>(
                categoryService.getCategories(page, size),
                HttpStatus.OK
        );
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getCategory(@PathVariable String title)
    {
        return new ResponseEntity<>(
                categoryService.getCategory(title),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestBody CategoryRequestDto categoryRequestDto
    )
    {
        return new ResponseEntity<>(
                categoryService.createCategory(categoryRequestDto),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{title}")
    public ResponseEntity<?> updateCategory(
            @PathVariable String title,
            @RequestBody CategoryRequestDto categoryDto
    )
    {
        return new ResponseEntity<>(
                categoryService.updateCategory(title, categoryDto),
                HttpStatus.OK
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{title}")
    public void deleteCategory(@PathVariable String title)
    {
        categoryService.deleteCategory(title);
    }
}