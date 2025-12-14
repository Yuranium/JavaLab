package com.javalab.taskservice.mapper;

import com.javalab.taskservice.dto.CategoryResponseDto;
import com.javalab.taskservice.tables.records.CategoryRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Collection;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper
{
    CategoryResponseDto toResponseDto(CategoryRecord category);

    Collection<CategoryResponseDto> toResponseDto(Collection<CategoryRecord> categories);
}