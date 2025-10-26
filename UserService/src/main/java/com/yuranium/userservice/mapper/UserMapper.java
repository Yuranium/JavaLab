package com.yuranium.userservice.mapper;

import com.yuranium.userservice.models.dto.UserDto;
import com.yuranium.userservice.models.dto.UserInputDto;
import com.yuranium.userservice.models.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper
{
    UserEntity toEntity(UserDto userDto);

    UserDto toDto(UserEntity userEntity);

    Iterable<UserDto> toDto(Iterable<UserEntity> userEntityList);

    Iterable<UserEntity> toEntity(Iterable<UserDto> userDtoList);

    UserEntity toEntity(UserInputDto userInputDto);
}