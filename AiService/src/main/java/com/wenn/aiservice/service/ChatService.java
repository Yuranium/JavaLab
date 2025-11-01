package com.wenn.aiservice.service;

import com.wenn.aiservice.util.ChatProcessingException;
import com.wenn.aiservice.util.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatClient chatClient;

    public String chat(String conversationId, String userMessage) {
        validateConversationId(conversationId);
        validateUserMessage(userMessage);

        try {
            return chatClient
                    .prompt()
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception ex) {
            throw new ChatProcessingException("Failed to process chat request", ex);
        }
    }

    private void validateConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new InvalidRequestException("Field 'userId' is required");
        }
    }

    private void validateUserMessage(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            throw new InvalidRequestException("Field 'message' is required");
        }
    }
}
