package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, String> {

    Optional<UserProgressEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
