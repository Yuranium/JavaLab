package com.wenn.progressservice.repository;

import com.wenn.progressservice.models.entity.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, UUID> {

    Optional<UserProgressEntity> findByKeycloakId(UUID keycloakId);

    boolean existsByKeycloakId(UUID keycloakId);
}
