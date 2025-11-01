package com.wenn.aiservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final JdbcChatMemoryRepository chatMemoryRepository;

    @Bean
    public ChatMemory chatMemory() {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(100)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {

        var memoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        return chatClientBuilder
                .defaultAdvisors(memoryAdvisor)
                .build();
    }
}
