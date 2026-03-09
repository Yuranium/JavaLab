package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
    Optional<UserEntity> findByUsername(String username);

    @Modifying
    @Query(value = """
            DELETE FROM user_background ub
            WHERE ub.activity = false
              AND ub.last_login IS NULL
              AND ub.date_registration <= :expiration_time
            """, nativeQuery = true)
    @Transactional
    void deleteInactiveUsers(@Param("expiration_time") LocalDateTime expirationTime);

    @Query(value = "SELECT u.email FROM UserEntity u")
    Iterable<String> findAllEmails(Pageable pageable);
}