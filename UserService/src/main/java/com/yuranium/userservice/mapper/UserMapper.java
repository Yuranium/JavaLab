package com.yuranium.userservice.mapper;

import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.models.dto.UserResponseDto;
import com.yuranium.userservice.models.dto.UserUpdateDto;
import com.yuranium.userservice.models.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper
{
    @Mapping(target = "dateRegistration", source = "background.dateRegistration")
    @Mapping(target = "activity", source = "background.activity")
    @Mapping(target = "timezone", source = "background.timezone")
    @Mapping(target = "lastLogin", source = "background.lastLogin")
    UserResponseDto toResponseDto(UserEntity userEntity);

    Iterable<UserResponseDto> toResponseDto(Iterable<UserEntity> userEntityList);

    @Mapping(target = "avatar", ignore = true)
    UserEntity toEntity(UserRequestDto userRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "background", ignore = true)
    @Mapping(target = "authMethods", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget UserEntity userEntity, UserUpdateDto userUpdateDto);
}