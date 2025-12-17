package com.javalab.taskservice.mapper;

import com.javalab.taskservice.dto.response.TestCaseResponseDto;
import com.javalab.taskservice.tables.records.TestCaseRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Collection;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TestCaseMapper
{
    TestCaseResponseDto toResponseDto(TestCaseRecord testCase);

    Collection<TestCaseResponseDto> toResponseDto(Collection<TestCaseRecord> testCases);
}