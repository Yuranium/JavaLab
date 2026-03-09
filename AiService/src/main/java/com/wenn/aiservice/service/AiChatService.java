package com.wenn.aiservice.service;

import com.wenn.aiservice.models.entity.AiChat;
import com.wenn.aiservice.models.entity.AiChatMessage;
import com.wenn.aiservice.repository.AiChatMessageRepository;
import com.wenn.aiservice.repository.AiChatRepository;
import com.wenn.aiservice.util.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatRepository chatRepository;
    private final AiChatMessageRepository messageRepository;

    public AiChat createChat(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidRequestException("ID cannot be null or empty");
        }
        AiChat chat = AiChat.builder().id(id).build();
        return chatRepository.save(chat);
    }

    public AiChat getChat(String id) {
        Optional<AiChat> byId = chatRepository.findById(id);
        return byId.orElseGet(() -> createChat(id));
    }

    public void saveMessages(List<AiChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return;
        messageRepository.saveAll(messages);
    }

    public List<AiChatMessage> getLastMessages(String chatId, int max) {
        if (chatId == null) return Collections.emptyList();
        List<AiChatMessage> desc = messageRepository.findByAiChat_IdOrderByCreatedAtDesc(chatId, PageRequest.of(0, Math.max(1, max)));
        Collections.reverse(desc);
        return desc;
    }

    @Transactional
    public void clearChatMessages(String chatId) {
        if (chatId == null) return;
        messageRepository.deleteByAiChat_Id(chatId);
    }
}