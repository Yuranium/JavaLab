package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.UserSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubmissionRepository extends JpaRepository<UserSubmissionEntity, Long> {

    Page<UserSubmissionEntity> findByUserProgressUsername(String username, Pageable pageable);

    Page<UserSubmissionEntity> findByUserProgressUsernameAndIdTask(
            String username,
            Long idTask,
            Pageable pageable
    );

    List<UserSubmissionEntity> findByUserProgressUsernameAndIdTaskOrderByAttemptNumberDesc(
            String username,
            Long idTask
    );

    Optional<UserSubmissionEntity> findFirstByUserProgressUsernameAndIdTaskOrderByAttemptNumberDesc(
            String username,
            Long idTask
    );

    int countByUserProgressUsernameAndIdTask(String username, Long idTask);

    List<UserSubmissionEntity> findByUserProgressUsernameAndIsCorrectTrue(String username);
}
