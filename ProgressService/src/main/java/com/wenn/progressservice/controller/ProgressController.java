package com.wenn.progressservice.controller;

import com.wenn.progressservice.dto.response.AchievementResponseDto;
import com.wenn.progressservice.dto.response.DailyActivityResponseDto;
import com.wenn.progressservice.dto.response.ProgressResponseDto;
import com.wenn.progressservice.dto.response.SubmissionResponseDto;
import com.wenn.progressservice.mapper.ProgressMapper;
import com.wenn.progressservice.service.AchievementService;
import com.wenn.progressservice.service.ProgressService;
import com.wenn.progressservice.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST контроллер для управления прогрессом пользователя.
 * 
 * Все endpoints требуют аутентификации через JWT токен.
 * keycloakId извлекается из токена (@AuthenticationPrincipal Jwt).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final AchievementService achievementService;
    private final SubmissionService submissionService;
    private final ProgressMapper mapper;

    /**
     * Получить прогресс текущего пользователя.
     * 
     * @param jwt JWT токен аутентификации
     * @return прогресс пользователя
     */
    @GetMapping
    public ResponseEntity<ProgressResponseDto> getProgress(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        var progress = progressService.getProgress(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User progress not found: " + keycloakId));
        
        return ResponseEntity.ok(mapper.toProgressResponseDto(progress));
    }

    /**
     * Получить достижения текущего пользователя.
     * 
     * @param jwt JWT токен аутентификации
     * @return список достижений
     */
    @GetMapping("/achievements")
    public ResponseEntity<Page<AchievementResponseDto>> getAchievements(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        var achievements = achievementService.getUserAchievements(keycloakId);
        
        return ResponseEntity.ok(achievements.map(mapper::toAchievementResponseDto));
    }

    /**
     * Получить ежедневную активность текущего пользователя за период.
     * 
     * @param jwt JWT токен аутентификации
     * @param from дата начала периода (опционально, по умолчанию 30 дней назад)
     * @param to дата окончания периода (опционально, по умолчанию сегодня)
     * @return список записей активности
     */
    @GetMapping("/activity")
    public ResponseEntity<Page<DailyActivityResponseDto>> getActivity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());

        // Если даты не указаны, берём период за последние 30 дней
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        // Используем метод сервиса для получения активности за период
        var activities = progressService.getActivityByPeriod(keycloakId, from, to);

        return ResponseEntity.ok(activities.map(mapper::toDailyActivityResponseDto));
    }

    /**
     * Получить историю попыток решения задач текущего пользователя.
     * 
     * @param jwt JWT токен аутентификации
     * @param taskId ID задачи (опционально, если не указано — все попытки)
     * @param page номер страницы (опционально, по умолчанию 0)
     * @param size размер страницы (опционально, по умолчанию 20)
     * @return страница попыток
     */
    @GetMapping("/submissions")
    public ResponseEntity<Page<SubmissionResponseDto>> getSubmissions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        
        Page<SubmissionResponseDto> submissions;
        if (taskId != null) {
            submissions = submissionService.getSubmissionsForTask(keycloakId, taskId, pageable)
                    .map(mapper::toSubmissionResponseDto);
        } else {
            submissions = submissionService.getSubmissions(keycloakId, pageable)
                    .map(mapper::toSubmissionResponseDto);
        }
        
        return ResponseEntity.ok(submissions);
    }

    /**
     * Получить разблокированные достижения текущего пользователя.
     * 
     * @param jwt JWT токен аутентификации
     * @return список разблокированных достижений
     */
    @GetMapping("/achievements/unlocked")
    public ResponseEntity<List<AchievementResponseDto>> getUnlockedAchievements(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        var achievements = achievementService.getUnlockedAchievements(keycloakId);
        
        return ResponseEntity.ok(mapper.toAchievementResponseDtoList(achievements));
    }

    /**
     * Получить заблокированные достижения текущего пользователя.
     * 
     * @param jwt JWT токен аутентификации
     * @return список заблокированных достижений
     */
    @GetMapping("/achievements/locked")
    public ResponseEntity<List<AchievementResponseDto>> getLockedAchievements(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        var achievements = achievementService.getLockedAchievements(keycloakId);
        
        return ResponseEntity.ok(mapper.toAchievementResponseDtoList(achievements));
    }
}
