package com.wenn.progressservice.service.kafka;

import com.javalab.core.events.UserLoggedInEvent;
import com.javalab.core.events.UserRegisteredEvent;
import com.wenn.progressservice.service.AchievementService;
import com.wenn.progressservice.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka Consumer для обработки событий пользователей.
 * 
 * Обрабатывает:
 * - UserRegisteredEvent — регистрация нового пользователя
 * - UserLoggedInEvent — вход пользователя в систему
 * 
 * Делегирует бизнес-логику сервисам:
 * - ProgressService — обновление прогресса
 * - AchievementService — управление достижениями
 */
@Service
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
    topics = {
        "${kafka.topic-names.user-registered}",
        "${kafka.topic-names.user-logged-in}"
    },
    containerFactory = "kafkaListenerContainerFactory"
)
public class KafkaConsumer {

    private final ProgressService progressService;
    private final AchievementService achievementService;

    /**
     * Обработка события регистрации пользователя.
     * 
     * Вызывает:
     * - progressService.createUserProgress() — создание записи прогресса
     * - achievementService.initializeUserAchievements() — инициализация ачивок
     * 
     * @param event событие регистрации
     */
    @KafkaHandler
    public void consume(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for keycloakId: {}", event.keycloakId());
        
        UUID keycloakId = event.keycloakId();
        
        // Создаём прогресс пользователя
        progressService.createUserProgress(keycloakId);
        
        // Инициализируем ачивки
        achievementService.initializeUserAchievements(keycloakId);
        
        log.info("UserRegisteredEvent processed successfully for keycloakId: {}", keycloakId);
    }

    /**
     * Обработка события входа пользователя.
     * 
     * Вызывает:
     * - progressService.updateOnLogin() — обновление streak и daily_activity
     * - achievementService.checkLoginStreakAchievements() — проверка ачивок LOGIN_STREAK
     * 
     * @param event событие входа
     */
    @KafkaHandler
    public void consume(UserLoggedInEvent event) {
        log.info("Received UserLoggedInEvent for keycloakId: {}", event.keycloakUserId());
        
        UUID keycloakId = event.keycloakUserId();
        Instant loginTime = Instant.ofEpochSecond(event.loginTimestamp());
        
        // Обновляем прогресс (streak, daily_activity)
        progressService.updateOnLogin(keycloakId, loginTime);
        
        // Проверяем ачивки LOGIN_STREAK
        var progress = progressService.getProgress(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User progress not found: " + keycloakId));
        achievementService.checkLoginStreakAchievements(keycloakId, progress.getCurrentStreak());
        
        log.info("UserLoggedInEvent processed successfully for keycloakId: {}. Streak: {}", 
                keycloakId, progress.getCurrentStreak());
    }
}
