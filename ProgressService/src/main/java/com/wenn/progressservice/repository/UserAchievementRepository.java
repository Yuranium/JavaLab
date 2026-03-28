package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.UserAchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievementEntity, Long> {

    List<UserAchievementEntity> findByUserProgressKeycloakId(UUID keycloakId);

    Optional<UserAchievementEntity> findByUserProgressKeycloakIdAndAchievementCode(
            UUID keycloakId,
            String achievementCode
    );

    List<UserAchievementEntity> findByUserProgressKeycloakIdAndUnlockedTrue(UUID keycloakId);

    List<UserAchievementEntity> findByUserProgressKeycloakIdAndUnlockedFalse(UUID keycloakId);
}
