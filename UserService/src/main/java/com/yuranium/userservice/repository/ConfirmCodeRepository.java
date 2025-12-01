package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.ConfirmationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmCodeRepository extends JpaRepository<ConfirmationCodeEntity, Long>
{
    Optional<ConfirmationCodeEntity> findByUserIdAndCode(Long userId, String code);
}