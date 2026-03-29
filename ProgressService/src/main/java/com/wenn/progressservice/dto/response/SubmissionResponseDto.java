package com.wenn.progressservice.dto.response;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO для ответа с информацией о попытке решения задачи.
 */
public record SubmissionResponseDto(
    Integer attemptNumber,
    Long taskId,
    String userCode,
    Boolean isCorrect,
    Instant submittedAt
) implements Serializable {}
