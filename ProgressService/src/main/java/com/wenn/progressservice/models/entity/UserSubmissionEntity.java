package com.wenn.progressservice.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_submission", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keycloak_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_submissions_user_progress"))
    private UserProgressEntity userProgress;

    @Column(name = "id_task", nullable = false)
    private Long idTask;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "user_code", nullable = false, columnDefinition = "TEXT")
    private String userCode;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @PrePersist
    public void prePersist() {
        if (isCorrect == null) isCorrect = false;
    }
}
