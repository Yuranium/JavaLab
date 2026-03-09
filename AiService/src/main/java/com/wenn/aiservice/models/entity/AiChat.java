package com.wenn.aiservice.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ai_chat")
public class AiChat {

    /**
     * Храним conversationId (userId из body) как строку — избегаем парсинга/ошибок.
     */
    @Id
    @Column(name = "chat_id", nullable = false, length = 128)
    private String id;

    @OneToMany(mappedBy = "aiChat", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<AiChatMessage> aiChatMessages = new ArrayList<>();

    @CreationTimestamp
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
    private ZonedDateTime updatedAt;
}