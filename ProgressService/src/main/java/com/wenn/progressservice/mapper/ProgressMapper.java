package com.wenn.progressservice.mapper;

import com.wenn.progressservice.dto.response.AchievementResponseDto;
import com.wenn.progressservice.dto.response.DailyActivityResponseDto;
import com.wenn.progressservice.dto.response.ProgressResponseDto;
import com.wenn.progressservice.dto.response.SubmissionResponseDto;
import com.wenn.progressservice.models.entity.AchievementEntity;
import com.wenn.progressservice.models.entity.DailyActivityEntity;
import com.wenn.progressservice.models.entity.UserAchievementEntity;
import com.wenn.progressservice.models.entity.UserProgressEntity;
import com.wenn.progressservice.models.entity.UserSubmissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper для конвертации Entity ↔ DTO.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProgressMapper {

    // ==================== Progress ====================

    ProgressResponseDto toProgressResponseDto(UserProgressEntity entity);

    // ==================== Achievement ====================

    @Mapping(target = "id", source = "achievement.id")
    @Mapping(target = "code", source = "achievement.code")
    @Mapping(target = "name", source = "achievement.name")
    @Mapping(target = "description", source = "achievement.description")
    @Mapping(target = "iconUrl", source = "achievement.iconUrl")
    @Mapping(target = "unlocked", source = "unlocked")
    @Mapping(target = "unlockedAt", source = "unlockedAt")
    AchievementResponseDto toAchievementResponseDto(UserAchievementEntity entity);

    List<AchievementResponseDto> toAchievementResponseDtoList(List<UserAchievementEntity> entities);

    // ==================== DailyActivity ====================

    DailyActivityResponseDto toDailyActivityResponseDto(DailyActivityEntity entity);

    // ==================== Submission ====================

    SubmissionResponseDto toSubmissionResponseDto(UserSubmissionEntity entity);

    List<SubmissionResponseDto> toSubmissionResponseDtoList(List<UserSubmissionEntity> entities);
}
