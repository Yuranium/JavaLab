package com.javalab.taskservice.mapper;

import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
import com.javalab.taskservice.tables.records.StarterCodeRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StarterCodeMapper
{
    StarterCodeResponseDto toResponseDto(StarterCodeRecord starterCode);
}