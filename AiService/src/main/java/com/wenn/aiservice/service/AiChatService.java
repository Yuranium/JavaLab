package com.wenn.aiservice.service;

import com.wenn.aiservice.models.entity.AiChat;
import com.wenn.aiservice.repository.AiChatRepository;
import com.wenn.aiservice.util.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiChatService {


    private final AiChatRepository chatRepository;

    public AiChat createChat(Long id) {
        return chatRepository.save(
                AiChat.builder()
                        .id(Optional.ofNullable(id)
                                .orElseThrow(() -> new InvalidRequestException("ID cannot be null")))
                        .build()
        );
    }

    public AiChat getChat(Long id) {

        Optional<AiChat> byId = chatRepository.findById(Math.toIntExact(id));

        return byId.orElseGet(() -> createChat(id));
    }

    public void saveChat(AiChat chat) {
        chatRepository.save(chat);
    }

    @Transactional
    public void clearChatMessages(Long id) {
        AiChat aiChat = getChat(id);
        if (aiChat == null) {
            return;
        }
        if (aiChat.getAiChatMessages() != null) {
            aiChat.getAiChatMessages().clear();
        }
        chatRepository.save(aiChat);
    }

}
