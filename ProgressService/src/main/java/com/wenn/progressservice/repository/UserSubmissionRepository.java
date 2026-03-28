package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.UserSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubmissionRepository extends JpaRepository<UserSubmissionEntity, Long> {

    Page<UserSubmissionEntity> findByUserProgressKeycloakId(UUID keycloakId, Pageable pageable);

    Page<UserSubmissionEntity> findByUserProgressKeycloakIdAndIdTask(
            UUID keycloakId,
            Long idTask,
            Pageable pageable
    );

    List<UserSubmissionEntity> findByUserProgressKeycloakIdAndIdTaskOrderByAttemptNumberDesc(
            UUID keycloakId,
            Long idTask
    );

    Optional<UserSubmissionEntity> findFirstByUserProgressKeycloakIdAndIdTaskOrderByAttemptNumberDesc(
            UUID keycloakId,
            Long idTask
    );

    int countByUserProgressKeycloakIdAndIdTask(UUID keycloakId, Long idTask);

    List<UserSubmissionEntity> findByUserProgressKeycloakIdAndIsCorrectTrue(UUID keycloakId);
}
