package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<AuthEntity, Long> {}