package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.UserAchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievementEntity, Long> {

    List<UserAchievementEntity> findByUserProgressUsername(String username);

    Optional<UserAchievementEntity> findByUserProgressUsernameAndAchievementCode(
            String username,
            String achievementCode
    );

    List<UserAchievementEntity> findByUserProgressUsernameAndUnlockedTrue(String username);

    List<UserAchievementEntity> findByUserProgressUsernameAndUnlockedFalse(String username);
}
