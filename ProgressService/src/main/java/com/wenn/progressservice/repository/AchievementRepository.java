package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<AchievementEntity, Long> {

    List<AchievementEntity> findByAchievementType(String achievementType);

    List<AchievementEntity> findByAchievementTypeAndThresholdLessThanEqual(
            String achievementType,
            Integer threshold
    );
}
