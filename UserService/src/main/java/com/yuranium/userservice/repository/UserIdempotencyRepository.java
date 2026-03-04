package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.UserIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserIdempotencyRepository extends JpaRepository<UserIdempotencyEntity, UUID>
{
    @Modifying
    @Query(value = """
            DELETE FROM user_idempotency_key uik
            WHERE uik.created_at <= :expiration_time
            """, nativeQuery = true)
    void deleteExpiredIdempotencyKey(@Param("expiration_time") LocalDateTime expirationTime);
}