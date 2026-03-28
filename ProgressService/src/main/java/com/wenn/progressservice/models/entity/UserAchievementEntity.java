package com.wenn.progressservice.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user_achievement", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", nullable = false, foreignKey = @ForeignKey(name = "fk_user_achievements_user_progress"))
    private UserProgressEntity userProgress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_achievement", nullable = false, foreignKey = @ForeignKey(name = "fk_user_achievements_achievement"))
    private AchievementEntity achievement;

    @Column(name = "unlocked", nullable = false)
    @Builder.Default
    private Boolean unlocked = false;

    @CreationTimestamp
    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @PrePersist
    public void prePersist() {
        if (unlocked == null) unlocked = false;
    }
}
