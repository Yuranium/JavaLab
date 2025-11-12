package com.wenn.aiservice.config;

import com.wenn.aiservice.enums.AiRole;
import com.wenn.aiservice.models.entity.AiChat;
import com.wenn.aiservice.models.entity.AiChatMessage;
import com.wenn.aiservice.service.AiChatService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;


@RequiredArgsConstructor
@Builder
public class CustomPostgresChatMemory implements ChatMemory {

    private final AiChatService aiChatService;
    private final int maxMessages;

    @Override
    public void add(String conversationId, List<Message> messages) {
        AiChat aiChat = aiChatService.getChat(Long.parseLong(conversationId));

        for (Message message : messages) {

            AiChatMessage aiChatMessage = AiChatMessage.builder()
                    .aiChat(aiChat)
                    .content(message.getText())
                    .aiRole(getAiRole(message))
                    .build();

            aiChat.getAiChatMessages().add(aiChatMessage);
        }

        aiChatService.saveChat(aiChat);
    }


    @Override
    public List<Message> get(String conversationId) {
        AiChat aiChat = aiChatService.getChat(Long.parseLong(conversationId));

        return aiChat.getAiChatMessages().stream()
                .skip(Math.max(0, aiChat.getAiChatMessages().size() - maxMessages))
                .map(this::getMessage)
                .limit(maxMessages)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        final long chatId;
        try {
            chatId = Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            return;
        }

        aiChatService.clearChatMessages(chatId);
    }

    private Message getMessage(AiChatMessage aiChatMessage) {
        switch (aiChatMessage.getAiRole()) {
            case USER -> {
                return new UserMessage(aiChatMessage.getContent());
            }
            case ASSISTANT -> {
                return new AssistantMessage(aiChatMessage.getContent());
            }
            case SYSTEM -> {
                return new SystemMessage(aiChatMessage.getContent());
            }
            default -> {
                return null;
            }
        }
    }

    private AiRole getAiRole(Message message) {
        switch (message.getMessageType()){
            case USER -> {
                return AiRole.USER;
            }
            case ASSISTANT -> {
                return AiRole.ASSISTANT;
            }
            case SYSTEM -> {
                return AiRole.SYSTEM;
            }
            default -> {
                return null;
            }
        }
    }
}
