package com.wenn.progressservice.dto.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для ответа с прогрессом пользователя.
 */
public record ProgressResponseDto(
    UUID keycloakId,
    Long totalTasksSolved,
    Long totalAttempts,
    Integer currentStreak,
    Integer longestStreak,
    LocalDate lastLoginDate,
    LocalDate lastActivityDate
) implements Serializable {}
