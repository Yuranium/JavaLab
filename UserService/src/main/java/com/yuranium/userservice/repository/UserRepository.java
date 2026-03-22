package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends
        JpaRepository<UserEntity, Long>,JpaSpecificationExecutor<UserEntity>
{
    @Modifying
    @Query(value = """
            DELETE FROM user_background ub
            WHERE ub.activity = false
              AND ub.last_login IS NULL
              AND ub.date_registration <= :expiration_time
            """, nativeQuery = true)
    @Transactional
    void deleteInactiveUsers(@Param("expiration_time") Instant expirationTime);

    @EntityGraph(attributePaths = "background")
    Optional<UserEntity> findByKeycloakId(UUID keycloakId);

    @EntityGraph(attributePaths = "background")
    Page<UserEntity> findAll(Specification<UserEntity> specification, Pageable pageable);

    @Query(value = "SELECT u.email FROM UserEntity u WHERE u.background.notifyEnabled = true")
    Page<String> findSuitableEmails(Pageable pageable);

    Boolean existsByUsernameOrEmail(String username, String email);
}