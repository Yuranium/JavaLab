package com.wenn.progressservice.service;

import com.wenn.progressservice.models.entity.DailyActivityEntity;
import com.wenn.progressservice.models.entity.UserProgressEntity;
import com.wenn.progressservice.repository.DailyActivityRepository;
import com.wenn.progressservice.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления прогрессом пользователя.
 * 
 * Ответственность:
 * - Создание прогресса при регистрации
 * - Обновление статистики (total_tasks_solved, total_attempts)
 * - Расчёт streak (входы + задачи)
 * - Обновление daily_activity
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository userProgressRepository;
    private final DailyActivityRepository dailyActivityRepository;

    /**
     * Создаёт новую запись прогресса для зарегистрированного пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @return созданная запись прогресса
     */
    @Transactional
    public UserProgressEntity createUserProgress(UUID keycloakId) {
        log.info("Creating user progress for keycloakId: {}", keycloakId);
        
        // Проверяем существует ли уже пользователь
        if (userProgressRepository.existsByKeycloakId(keycloakId)) {
            log.info("User progress already exists for keycloakId: {}", keycloakId);
            return userProgressRepository.findByKeycloakId(keycloakId).get();
        }
        
        UserProgressEntity progress = UserProgressEntity.builder()
                .keycloakId(keycloakId)
                .totalTasksSolved(0L)
                .totalAttempts(0L)
                .currentStreak(0)
                .longestStreak(0)
                .build();
        
        userProgressRepository.save(progress);
        log.info("User progress created for keycloakId: {}", keycloakId);
        
        return progress;
    }

    /**
     * Обновляет прогресс при входе пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param loginTime время входа
     * @return обновлённая запись прогресса
     */
    @Transactional
    public UserProgressEntity updateOnLogin(UUID keycloakId, java.time.Instant loginTime) {
        log.info("Updating progress on login for keycloakId: {}", keycloakId);
        
        UserProgressEntity progress = userProgressRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + keycloakId));
        
        // Получаем дату входа в часовом поясе Moscow
        LocalDate loginDate = loginTime.atZone(ZoneId.of("Europe/Moscow")).toLocalDate();
        
        // Обновляем daily_activity.login_count
        updateDailyActivityLogin(progress, loginDate);
        
        // Обновляем streak входов
        updateLoginStreak(progress, loginDate);
        
        log.info("Login streak updated for keycloakId: {}. Current streak: {}", 
                keycloakId, progress.getCurrentStreak());
        
        return progress;
    }

    /**
     * Обновляет прогресс при отправке решения задачи.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @param isCorrect результат проверки (true = верно)
     * @param submissionTime время отправки решения
     * @return обновлённая запись прогресса
     */
    @Transactional
    public UserProgressEntity updateOnTaskSubmission(
            UUID keycloakId, 
            Long idTask, 
            boolean isCorrect,
            java.time.Instant submissionTime
    ) {
        log.info("Updating progress on task submission for keycloakId: {}, task: {}, correct: {}", 
                keycloakId, idTask, isCorrect);
        
        UserProgressEntity progress = userProgressRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + keycloakId));
        
        // Получаем дату отправки в часовом поясе Moscow
        LocalDate submissionDate = submissionTime.atZone(ZoneId.of("Europe/Moscow")).toLocalDate();
        
        // Обновляем total_attempts
        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        
        if (isCorrect) {
            // Обновляем total_tasks_solved
            progress.setTotalTasksSolved(progress.getTotalTasksSolved() + 1);
            
            // Обновляем daily_activity
            updateDailyActivityTask(progress, submissionDate);
            
            // Обновляем streak задач
            updateTaskStreak(progress, submissionDate);
        } else {
            // Обновляем только attempts_count в daily_activity
            updateDailyActivityAttempt(progress, submissionDate);
        }
        
        userProgressRepository.save(progress);
        
        log.info("Task submission progress updated for keycloakId: {}. Total solved: {}", 
                keycloakId, progress.getTotalTasksSolved());
        
        return progress;
    }

    /**
     * Обновляет login_count в daily_activity.
     */
    private void updateDailyActivityLogin(UserProgressEntity progress, LocalDate date) {
        DailyActivityEntity activity = dailyActivityRepository
                .findByUserProgressKeycloakIdAndActivityDate(progress.getKeycloakId(), date)
                .orElse(DailyActivityEntity.builder()
                        .userProgress(progress)
                        .activityDate(date)
                        .tasksSolved(0)
                        .attemptsCount(0)
                        .loginCount(0)
                        .build());
        
        activity.setLoginCount(activity.getLoginCount() + 1);
        dailyActivityRepository.save(activity);
        log.debug("Daily activity login_count updated for keycloakId: {} on date: {}", 
                progress.getKeycloakId(), date);
    }

    /**
     * Обновляет tasks_solved в daily_activity.
     */
    private void updateDailyActivityTask(UserProgressEntity progress, LocalDate date) {
        DailyActivityEntity activity = dailyActivityRepository
                .findByUserProgressKeycloakIdAndActivityDate(progress.getKeycloakId(), date)
                .orElse(DailyActivityEntity.builder()
                        .userProgress(progress)
                        .activityDate(date)
                        .tasksSolved(0)
                        .attemptsCount(0)
                        .loginCount(0)
                        .build());
        
        activity.setTasksSolved(activity.getTasksSolved() + 1);
        activity.setAttemptsCount(activity.getAttemptsCount() + 1);
        dailyActivityRepository.save(activity);
    }

    /**
     * Обновляет attempts_count в daily_activity.
     */
    private void updateDailyActivityAttempt(UserProgressEntity progress, LocalDate date) {
        DailyActivityEntity activity = dailyActivityRepository
                .findByUserProgressKeycloakIdAndActivityDate(progress.getKeycloakId(), date)
                .orElse(DailyActivityEntity.builder()
                        .userProgress(progress)
                        .activityDate(date)
                        .tasksSolved(0)
                        .attemptsCount(0)
                        .loginCount(0)
                        .build());
        
        activity.setAttemptsCount(activity.getAttemptsCount() + 1);
        dailyActivityRepository.save(activity);
    }

    /**
     * Обновляет streak входов.
     * 
     * Логика:
     * - Если последний вход был вчера → streak++
     * - Если последний вход был сегодня → streak не меняем
     * - Если последний вход был больше дня назад → streak = 1
     */
    private void updateLoginStreak(UserProgressEntity progress, LocalDate loginDate) {
        LocalDate lastLoginDate = progress.getLastLoginDate();
        
        if (lastLoginDate == null) {
            // Первый вход
            progress.setCurrentStreak(1);
            progress.setLongestStreak(1);
        } else if (lastLoginDate.equals(loginDate.minusDays(1))) {
            // Вход на следующий день после последнего → увеличиваем streak
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
            progress.setLongestStreak(Math.max(progress.getLongestStreak(), progress.getCurrentStreak()));
        } else if (lastLoginDate.isBefore(loginDate.minusDays(1))) {
            // Пропустили день или больше → сбрасываем streak
            progress.setCurrentStreak(1);
        }
        // Если lastLoginDate == loginDate (сегодня уже был вход) → streak не меняем
        
        progress.setLastLoginDate(loginDate);
        userProgressRepository.save(progress);
    }

    /**
     * Обновляет streak задач.
     * 
     * Логика:
     * - Если последняя активность была вчера → streak++
     * - Если последняя активность была сегодня → streak не меняем
     * - Если последняя активность была больше дня назад → streak = 1
     */
    private void updateTaskStreak(UserProgressEntity progress, LocalDate activityDate) {
        LocalDate lastActivityDate = progress.getLastActivityDate();
        
        if (lastActivityDate == null) {
            // Первая активность
            progress.setCurrentStreak(1);
            progress.setLongestStreak(1);
        } else if (lastActivityDate.equals(activityDate.minusDays(1))) {
            // Активность на следующий день → увеличиваем streak
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
            progress.setLongestStreak(Math.max(progress.getLongestStreak(), progress.getCurrentStreak()));
        } else if (lastActivityDate.isBefore(activityDate.minusDays(1))) {
            // Пропустили день или больше → сбрасываем streak
            progress.setCurrentStreak(1);
        }
        // Если lastActivityDate == activityDate (сегодня уже была активность) → streak не меняем
        
        progress.setLastActivityDate(activityDate);
        userProgressRepository.save(progress);
    }

    /**
     * Получает прогресс пользователя.
     */
    public Optional<UserProgressEntity> getProgress(UUID keycloakId) {
        return userProgressRepository.findByKeycloakId(keycloakId);
    }
}
