package com.wenn.progressservice.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_activity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_activity", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keycloak_id", nullable = false, foreignKey = @ForeignKey(name = "fk_daily_activity_user_progress"))
    private UserProgressEntity userProgress;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "tasks_solved", nullable = false)
    @Builder.Default
    private Integer tasksSolved = 0;

    @Column(name = "attempts_count", nullable = false)
    @Builder.Default
    private Integer attemptsCount = 0;

    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private Integer loginCount = 0;

    @PrePersist
    public void prePersist() {
        if (tasksSolved == null) tasksSolved = 0;
        if (attemptsCount == null) attemptsCount = 0;
        if (loginCount == null) loginCount = 0;
    }
}
