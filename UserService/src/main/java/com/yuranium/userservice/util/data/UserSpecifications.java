package com.yuranium.userservice.util.data;

import com.yuranium.userservice.models.entity.UserEntity;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications
{
    public static Specification<UserEntity> hasActivity(Boolean activity)
    {
        return (root, query, criteriaBuilder) -> {
            if (activity == null)
                return criteriaBuilder.conjunction();

            var join = root.join("background", JoinType.INNER);
            return criteriaBuilder.equal(join.get("activity"), activity);
        };
    }

    public static Specification<UserEntity> hasNotifyEnabled(Boolean notifyEnabled)
    {
        return (root, query, criteriaBuilder) -> {
            if (notifyEnabled == null)
                return criteriaBuilder.conjunction();

            var join = root.join("background", JoinType.INNER);
            return criteriaBuilder.equal(join.get("notifyEnabled"), notifyEnabled);
        };
    }
}