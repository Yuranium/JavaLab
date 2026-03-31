package com.yuranium.userservice.mapper;

import com.javalab.core.events.ExternalAuthEvent;
import com.yuranium.userservice.enums.ProviderType;
import com.yuranium.userservice.models.dto.PublicUserResponseDto;
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
    @Mapping(target = "notifyEnabled", source = "background.notifyEnabled")
    UserResponseDto toResponseDto(UserEntity userEntity);

    @Mapping(target = "dateRegistration", source = "background.dateRegistration")
    @Mapping(target = "activity", source = "background.activity")
    @Mapping(target = "lastLogin", source = "background.lastLogin")
    @Mapping(target = "notifyEnabled", source = "background.notifyEnabled")
    PublicUserResponseDto toPublicResponseDto(UserEntity userEntity);

    @Mapping(target = "avatar", ignore = true)
    UserEntity toEntity(UserRequestDto userRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "background", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget UserEntity userEntity, UserUpdateDto userUpdateDto);

    @AfterMapping
    default void updateBackground(@MappingTarget UserEntity userEntity, UserUpdateDto userUpdateDto)
    {
        if (userUpdateDto == null)
            return;

        if (userUpdateDto.notifyEnabled() != null)
            userEntity.getBackground().setNotifyEnabled(userUpdateDto.notifyEnabled());
    }

    @Mapping(target = "avatar", source = "avatarUrl")
    @Mapping(target = "name", source = "firstName")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserEntity toEntity(ExternalAuthEvent event);

    @AfterMapping
    default void fullAvatarPath(@MappingTarget UserEntity user, ExternalAuthEvent event)
    {
        if (event.avatarUrl() != null && !event.avatarUrl().isBlank())
        {
            ProviderType provider = ProviderType.fromId(event.providerId());
            user.setAvatar(provider.getAvatarUrl(event.avatarUrl()));
        }
    }
}