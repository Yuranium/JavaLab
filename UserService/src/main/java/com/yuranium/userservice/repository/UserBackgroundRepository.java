package com.yuranium.userservice.repository;

import com.yuranium.userservice.models.entity.UserBackgroundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBackgroundRepository extends JpaRepository<UserBackgroundEntity, Long> {}