package com.wenn.progressservice.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_achievement", nullable = false)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @Column(name = "achievement_type", nullable = false, length = 20)
    private String achievementType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "achievement", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAchievementEntity> userAchievements = new ArrayList<>();
}
