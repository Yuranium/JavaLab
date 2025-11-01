package com.yuranium.userservice.mapper;

import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper
{
    UserResponseDto toResponseDto(UserEntity userEntity);

    Iterable<UserResponseDto> toResponseDto(Iterable<UserEntity> userEntityList);

    Iterable<UserEntity> toEntity(Iterable<UserRequestDto> userDtoList);

    @Mapping(target = "avatar", ignore = true)
    UserEntity toEntity(UserRequestDto userRequestDto);
}