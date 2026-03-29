package com.wenn.progressservice.dto.response;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO для ответа с ежедневной активностью пользователя.
 */
public record DailyActivityResponseDto(
    LocalDate date,
    Integer tasksSolved,
    Integer attemptsCount,
    Integer loginCount
) implements Serializable {}
