package com.wenn.progressservice.dto.response;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO для ответа с информацией о достижении.
 */
public record AchievementResponseDto(
    Long id,
    String code,
    String name,
    String description,
    String iconUrl,
    Boolean unlocked,
    Instant unlockedAt
) implements Serializable {}
