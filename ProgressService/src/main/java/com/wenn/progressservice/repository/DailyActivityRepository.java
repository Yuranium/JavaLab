package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.DailyActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivityEntity, Long> {

    Optional<DailyActivityEntity> findByUserProgressKeycloakIdAndActivityDate(
            UUID keycloakId,
            LocalDate activityDate
    );

    List<DailyActivityEntity> findByUserProgressKeycloakIdOrderByActivityDateDesc(UUID keycloakId);

    List<DailyActivityEntity> findByUserProgressKeycloakIdAndActivityDateBetween(
            UUID keycloakId,
            LocalDate fromDate,
            LocalDate toDate
    );
}
