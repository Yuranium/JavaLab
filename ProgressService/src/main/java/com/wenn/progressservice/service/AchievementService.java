package com.wenn.progressservice.service;

import com.wenn.progressservice.enums.AchievementType;
import com.wenn.progressservice.models.entity.AchievementEntity;
import com.wenn.progressservice.models.entity.UserAchievementEntity;
import com.wenn.progressservice.models.entity.UserProgressEntity;
import com.wenn.progressservice.repository.AchievementRepository;
import com.wenn.progressservice.repository.UserAchievementRepository;
import com.wenn.progressservice.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления достижениями (ачивками) пользователей.
 * 
 * Ответственность:
 * - Инициализация ачивок при регистрации пользователя
 * - Проверка и активация ачивок при достижении порога
 * - Проверка ачивок LOGIN_STREAK, TASKS_SOLVED, TASK_STREAK
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * Инициализирует достижения для нового пользователя.
     * Создаёт записи в user_achievements для всех ачивок из справочника.
     * 
     * @param keycloakId ID пользователя в Keycloak
     */
    @Transactional
    public void initializeUserAchievements(UUID keycloakId) {
        log.info("Initializing user achievements for keycloakId: {}", keycloakId);
        
        UserProgressEntity progress = userProgressRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + keycloakId));
        
        // Получаем все ачивки из справочника
        List<AchievementEntity> allAchievements = achievementRepository.findAll();
        
        // Создаём записи в user_achievements (все locked)
        List<UserAchievementEntity> userAchievements = allAchievements.stream()
                .map(ach -> UserAchievementEntity.builder()
                        .userProgress(progress)
                        .achievement(ach)
                        .unlocked(false)
                        .build())
                .toList();
        
        userAchievementRepository.saveAll(userAchievements);
        log.info("Initialized {} achievements for keycloakId: {}", allAchievements.size(), keycloakId);
    }

    /**
     * Проверяет и активирует ачивки за серию входов (LOGIN_STREAK).
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param currentStreak текущая серия входов
     */
    @Transactional
    public void checkLoginStreakAchievements(UUID keycloakId, int currentStreak) {
        log.debug("Checking LOGIN_STREAK achievements for keycloakId: {}, streak: {}", keycloakId, currentStreak);
        
        // Находим все ачивки типа LOGIN_STREAK где threshold <= currentStreak
        List<AchievementEntity> eligibleAchievements = achievementRepository
                .findByAchievementTypeAndThresholdLessThanEqual(AchievementType.LOGIN_STREAK.name(), currentStreak);
        
        // Активируем те, которые ещё не активированы
        for (AchievementEntity achievement : eligibleAchievements) {
            unlockAchievement(keycloakId, achievement);
        }
        
        log.info("Checked LOGIN_STREAK achievements for keycloakId: {}. Unlocked: {}", 
                keycloakId, eligibleAchievements.size());
    }

    /**
     * Проверяет и активирует ачивки за количество решённых задач (TASKS_SOLVED).
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param tasksSolved количество решённых задач
     */
    @Transactional
    public void checkTaskAchievements(UUID keycloakId, long tasksSolved) {
        log.debug("Checking TASKS_SOLVED achievements for keycloakId: {}, tasksSolved: {}", keycloakId, tasksSolved);
        
        // Находим все ачивки типа TASKS_SOLVED где threshold <= tasksSolved
        List<AchievementEntity> eligibleAchievements = achievementRepository
                .findByAchievementTypeAndThresholdLessThanEqual(AchievementType.TASKS_SOLVED.name(), (int) Math.min(tasksSolved, Integer.MAX_VALUE));
        
        // Активируем те, которые ещё не активированы
        for (AchievementEntity achievement : eligibleAchievements) {
            unlockAchievement(keycloakId, achievement);
        }
        
        log.info("Checked TASKS_SOLVED achievements for keycloakId: {}. Unlocked: {}", 
                keycloakId, eligibleAchievements.size());
    }

    /**
     * Проверяет и активирует ачивки за серию дней с решёнными задачами (TASK_STREAK).
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param currentStreak текущая серия дней с задачами
     */
    @Transactional
    public void checkTaskStreakAchievements(UUID keycloakId, int currentStreak) {
        log.debug("Checking TASK_STREAK achievements for keycloakId: {}, streak: {}", keycloakId, currentStreak);
        
        // Находим все ачивки типа TASK_STREAK где threshold <= currentStreak
        List<AchievementEntity> eligibleAchievements = achievementRepository
                .findByAchievementTypeAndThresholdLessThanEqual(AchievementType.TASK_STREAK.name(), currentStreak);
        
        // Активируем те, которые ещё не активированы
        for (AchievementEntity achievement : eligibleAchievements) {
            unlockAchievement(keycloakId, achievement);
        }
        
        log.info("Checked TASK_STREAK achievements for keycloakId: {}. Unlocked: {}", 
                keycloakId, eligibleAchievements.size());
    }

    /**
     * Разблокирует достижение для пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param achievement достижение для разблокировки
     */
    private void unlockAchievement(UUID keycloakId, AchievementEntity achievement) {
        UserAchievementEntity userAchievement = userAchievementRepository
                .findByUserProgressKeycloakIdAndAchievementCode(keycloakId, achievement.getCode())
                .orElse(null);
        
        if (userAchievement != null && !userAchievement.getUnlocked()) {
            userAchievement.setUnlocked(true);
            userAchievement.setUnlockedAt(Instant.now());
            userAchievementRepository.save(userAchievement);
            log.info("Unlocked achievement '{}' for keycloakId: {}", achievement.getCode(), keycloakId);
        }
    }

    /**
     * Получает все достижения пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @return список достижений пользователя
     */
    @Transactional(readOnly = true)
    public Page<UserAchievementEntity> getUserAchievements(UUID keycloakId) {
        return userAchievementRepository.findByUserProgressKeycloakId(keycloakId);
    }

    /**
     * Получает разблокированные достижения пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @return список разблокированных достижений
     */
    @Transactional(readOnly = true)
    public List<UserAchievementEntity> getUnlockedAchievements(UUID keycloakId) {
        return userAchievementRepository.findByUserProgressKeycloakIdAndUnlockedTrue(keycloakId);
    }

    /**
     * Получает заблокированные достижения пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @return список заблокированных достижений
     */
    @Transactional(readOnly = true)
    public List<UserAchievementEntity> getLockedAchievements(UUID keycloakId) {
        return userAchievementRepository.findByUserProgressKeycloakIdAndUnlockedFalse(keycloakId);
    }
}
