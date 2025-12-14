package com.javalab.taskservice.service;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
import com.javalab.taskservice.mapper.StarterCodeMapper;
import com.javalab.taskservice.repository.StarterCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StarterCodeService
{
    private final StarterCodeRepository starterCodeRepository;

    private final StarterCodeMapper starterCodeMapper;

    public StarterCodeResponseDto createStarterCode(
            Long taskId, StarterCodeRequestDto starterCodeRequestDto
    )
    {
        return starterCodeMapper.toResponseDto(
                starterCodeRepository.createStarterCode(taskId, starterCodeRequestDto)
        );
    }
}