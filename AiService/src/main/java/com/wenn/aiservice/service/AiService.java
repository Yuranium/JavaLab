package com.wenn.aiservice.service;

import com.wenn.aiservice.config.CustomPostgresChatMemory;
import com.wenn.aiservice.util.ChatProcessingException;
import com.wenn.aiservice.util.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    private final AiChatService aiChatService;

    @Value("${app.maxMessages}")
    private int maxMessage;

    public List<Message> getHistory(String conversationId) {
        CustomPostgresChatMemory memory = CustomPostgresChatMemory.builder()
                .aiChatService(aiChatService)
                .maxMessages(maxMessage)
                .build();
        return memory.get(conversationId);
    }

    public void clearHistory(String conversationId) {
        validateConversationId(conversationId);
        CustomPostgresChatMemory memory = CustomPostgresChatMemory.builder()
                .aiChatService(aiChatService)
                .maxMessages(maxMessage)
                .build();
        memory.clear(conversationId);
    }

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

    public Flux<String> streamChat(String conversationId, String userMessage) {
        validateConversationId(conversationId);
        validateUserMessage(userMessage);

        try {
            return chatClient
                    .prompt()
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(userMessage)
                    .stream()
                    .content();
        } catch (Exception ex) {
            return Flux.error(new ChatProcessingException("Failed to start streaming chat", ex));
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
