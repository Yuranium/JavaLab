package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.DailyActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivityEntity, Long> {

    Optional<DailyActivityEntity> findByUserProgressUsernameAndActivityDate(
            String username,
            LocalDate activityDate
    );

    List<DailyActivityEntity> findByUserProgressUsernameOrderByActivityDateDesc(String username);

    List<DailyActivityEntity> findByUserProgressUsernameAndActivityDateBetween(
            String username,
            LocalDate fromDate,
            LocalDate toDate
    );
}
