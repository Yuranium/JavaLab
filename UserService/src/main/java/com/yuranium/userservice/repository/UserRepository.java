package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
    Optional<UserEntity> findByUsername(String username);

    @Modifying
    @Query(value = """
            DELETE FROM public.user u
            WHERE u.activity = false
              AND u.last_login IS NULL
              AND DATE_PART('day', AGE(u.date_registration)) >= 1
            """, nativeQuery = true)
    void deleteInactiveUsers();
}