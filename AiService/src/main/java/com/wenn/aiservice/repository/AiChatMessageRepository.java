package com.wenn.aiservice.repository;

import com.wenn.aiservice.models.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Integer> {
}
