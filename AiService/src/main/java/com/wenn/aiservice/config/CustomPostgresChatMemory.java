package com.wenn.aiservice.config;

import com.wenn.aiservice.enums.AiRole;
import com.wenn.aiservice.models.entity.AiChatMessage;
import com.wenn.aiservice.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomPostgresChatMemory implements ChatMemory {

    private final AiChatService aiChatService;
    private final int maxMessages;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank()) return;
        if (messages == null || messages.isEmpty()) return;

        var aiChat = aiChatService.getChat(conversationId);

        var entities = messages.stream()
                .map(m -> {
                    var msg = AiChatMessage.builder()
                            .aiChat(aiChat)
                            .content(m == null ? null : m.getText())
                            .aiRole(mapRoleSafe(m))
                            .build();
                    return msg;
                })
                .collect(Collectors.toList());

        aiChatService.saveMessages(entities);
    }

    @Override
    public List<Message> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return List.of();
        var last = aiChatService.getLastMessages(conversationId, maxMessages);
        return last.stream().map(this::toMessage).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return;
        aiChatService.clearChatMessages(conversationId);
    }

    private Message toMessage(AiChatMessage e) {
        if (e == null || e.getAiRole() == null) {
            return new SystemMessage("");
        }
        return switch (e.getAiRole()) {
            case USER -> new UserMessage(e.getContent());
            case ASSISTANT -> new AssistantMessage(e.getContent());
            case SYSTEM -> new SystemMessage(e.getContent());
        };
    }

    private AiRole mapRoleSafe(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }
        var mt = message.getMessageType();
        if (mt == null) {
            throw new IllegalArgumentException("message.getMessageType() is null");
        }

        switch (mt) {
            case USER:
                return AiRole.USER;
            case ASSISTANT:
                return AiRole.ASSISTANT;
            case SYSTEM:
                return AiRole.SYSTEM;
            default:
                throw new IllegalArgumentException("Unsupported Message.MessageType: " + mt);
        }
    }
}