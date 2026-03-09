package com.wenn.aiservice.repository;

import com.wenn.aiservice.models.entity.AiChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    /**
     * Возвращает последние сообщения для chatId в порядке createdAt DESC.
     * Мы реверсим в сервисе в хронологический порядок.
     */
    List<AiChatMessage> findByAiChat_IdOrderByCreatedAtDesc(String chatId, Pageable pageable);

    void deleteByAiChat_Id(String chatId);
}