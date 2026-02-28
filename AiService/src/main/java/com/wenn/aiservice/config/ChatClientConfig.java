package com.wenn.aiservice.config;

import com.wenn.aiservice.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.api.Advisor;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final AiChatService aiChatService;

    @Value("${app.maxMessages}")
    private int maxMessage;

    @Value("${app.systemPrompt}")
    private String systemPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        ChatMemory memory = new CustomPostgresChatMemory(aiChatService, maxMessage);
        Advisor advisor = MessageChatMemoryAdvisor.builder(memory).order(1).build();

        return chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(advisor)
                .build();
    }
}