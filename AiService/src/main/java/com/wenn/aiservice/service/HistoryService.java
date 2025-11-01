package com.wenn.aiservice.service;

import com.wenn.aiservice.util.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ChatMemory chatMemory;

    public List<Message> getHistory(String conversationId) {
        validateConversationId(conversationId);
        return chatMemory.get(conversationId);
    }

    public void clearHistory(String conversationId) {
        validateConversationId(conversationId);
        chatMemory.clear(conversationId);
    }

    private void validateConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new InvalidRequestException("Field 'userId' is required");
        }
    }
}
