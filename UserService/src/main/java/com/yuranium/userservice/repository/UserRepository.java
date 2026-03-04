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
            DELETE FROM user_background ub
            WHERE ub.activity = false
              AND ub.last_login IS NULL
              AND DATE_PART('day', AGE(ub.date_registration)) >= 1
            """, nativeQuery = true)
    void deleteInactiveUsers(); // todo изменить SQL запрос по аналогии с id-key
}