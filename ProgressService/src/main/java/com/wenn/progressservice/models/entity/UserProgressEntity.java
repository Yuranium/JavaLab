package com.wenn.progressservice.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressEntity {

    @Id
    @Column(name = "keycloak_id", nullable = false)
    private UUID keycloakId;

    @Column(name = "total_tasks_solved", nullable = false)
    @Builder.Default
    private Long totalTasksSolved = 0L;

    @Column(name = "total_attempts", nullable = false)
    @Builder.Default
    private Long totalAttempts = 0L;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "userProgress", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DailyActivityEntity> dailyActivities = new ArrayList<>();

    @OneToMany(mappedBy = "userProgress", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAchievementEntity> userAchievements = new ArrayList<>();

    @OneToMany(mappedBy = "userProgress", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSubmissionEntity> submissions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (totalTasksSolved == null) totalTasksSolved = 0L;
        if (totalAttempts == null) totalAttempts = 0L;
        if (currentStreak == null) currentStreak = 0;
        if (longestStreak == null) longestStreak = 0;
    }
}
