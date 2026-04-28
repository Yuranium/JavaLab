package com.wenn.progressservice.service;

import com.wenn.progressservice.models.entity.UserProgressEntity;
import com.wenn.progressservice.models.entity.UserSubmissionEntity;
import com.wenn.progressservice.repository.UserProgressRepository;
import com.wenn.progressservice.repository.UserSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления попытками решения задач пользователями.
 * 
 * Ответственность:
 * - Сохранение попытки решения
 * - Подсчёт номера попытки
 * - Получение истории попыток
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {

    private final UserSubmissionRepository submissionRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * Сохраняет попытку решения задачи.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @param userCode код решения пользователя
     * @param isCorrect результат проверки (true = верно)
     * @param submissionTime время отправки решения
     * @return сохранённая попытка
     */
    @Transactional
    public UserSubmissionEntity saveSubmission(
            UUID keycloakId,
            Long idTask,
            String userCode,
            boolean isCorrect,
            Instant submissionTime
    ) {
        log.info("Saving submission for keycloakId: {}, task: {}, correct: {}", 
                keycloakId, idTask, isCorrect);
        
        UserProgressEntity progress = userProgressRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + keycloakId));
        
        // Считаем номер попытки
        int attemptNumber = getAttemptNumber(keycloakId, idTask);
        
        UserSubmissionEntity submission = UserSubmissionEntity.builder()
                .userProgress(progress)
                .idTask(idTask)
                .attemptNumber(attemptNumber)
                .userCode(userCode)
                .isCorrect(isCorrect)
                .submittedAt(submissionTime)
                .build();
        
        submissionRepository.save(submission);
        log.info("Submission saved for keycloakId: {}, task: {}, attempt: {}", 
                keycloakId, idTask, attemptNumber);
        
        return submission;
    }

    /**
     * Получает номер следующей попытки для пользователя и задачи.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @return номер попытки (начинается с 1)
     */
    @Transactional(readOnly = true)
    public int getAttemptNumber(UUID keycloakId, Long idTask) {
        int count = submissionRepository.countByUserProgressKeycloakIdAndIdTask(keycloakId, idTask);
        return count + 1;
    }

    /**
     * Получает все попытки пользователя для конкретной задачи.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @return список попыток
     */
    @Transactional(readOnly = true)
    public List<UserSubmissionEntity> getSubmissionsForTask(UUID keycloakId, Long idTask) {
        return submissionRepository.findByUserProgressKeycloakIdAndIdTaskOrderByAttemptNumberDesc(
                keycloakId, idTask);
    }

    /**
     * Получает все попытки пользователя с пагинацией.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param pageable параметры пагинации
     * @return страница попыток
     */
    @Transactional(readOnly = true)
    public Page<UserSubmissionEntity> getSubmissions(UUID keycloakId, Pageable pageable) {
        return submissionRepository.findByUserProgressKeycloakId(keycloakId, pageable);
    }

    /**
     * Получает попытки пользователя для конкретной задачи с пагинацией.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @param pageable параметры пагинации
     * @return страница попыток
     */
    @Transactional(readOnly = true)
    public Page<UserSubmissionEntity> getSubmissionsForTask(
            UUID keycloakId, 
            Long idTask, 
            Pageable pageable
    ) {
        return submissionRepository.findByUserProgressKeycloakIdAndIdTask(
                keycloakId, idTask, pageable);
    }

    /**
     * Получает последнюю попытку пользователя для задачи.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @return последняя попытка или null
     */
    @Transactional(readOnly = true)
    public Optional<UserSubmissionEntity> getLastSubmission(UUID keycloakId, Long idTask) {
        return submissionRepository.findFirstByUserProgressKeycloakIdAndIdTaskOrderByAttemptNumberDesc(
                keycloakId, idTask);
    }

    /**
     * Получает все правильные попытки пользователя.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @return список правильных попыток
     */
    @Transactional(readOnly = true)
    public List<UserSubmissionEntity> getCorrectSubmissions(UUID keycloakId) {
        return submissionRepository.findByUserProgressKeycloakIdAndIsCorrectTrue(keycloakId);
    }

    /**
     * Получает количество попыток для задачи.
     * 
     * @param keycloakId ID пользователя в Keycloak
     * @param idTask ID задачи
     * @return количество попыток
     */
    @Transactional(readOnly = true)
    public int getSubmissionCount(UUID keycloakId, Long idTask) {
        return submissionRepository.countByUserProgressKeycloakIdAndIdTask(keycloakId, idTask);
    }
}
