package com.wenn.aiservice.repository;

import com.wenn.aiservice.models.entity.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatRepository extends JpaRepository<AiChat, Integer> {
}
