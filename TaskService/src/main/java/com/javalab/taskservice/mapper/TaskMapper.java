package com.javalab.taskservice.mapper;

import com.javalab.taskservice.dto.TaskResponseDto;
import com.javalab.taskservice.tables.records.TaskRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper
{
    TaskResponseDto toResponseDto(TaskRecord taskRecord);
}