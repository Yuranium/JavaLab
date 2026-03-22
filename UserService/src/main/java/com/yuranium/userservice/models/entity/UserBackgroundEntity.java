package com.yuranium.userservice.models.entity;

import com.yuranium.userservice.models.dto.UserRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_background")
@NoArgsConstructor
@AllArgsConstructor
public class UserBackgroundEntity
{
    @Id
    @Column(name = "id_background", columnDefinition = "BIGINT", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "date_registration", columnDefinition = "TIMESTAMP",  nullable = false)
    private Instant dateRegistration;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP")
    private Instant lastLogin;

    @Column(name = "timezone", columnDefinition = "VARCHAR(50)", nullable = false)
    private String timezone;

    @Column(name = "notify_enabled", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean notifyEnabled = true;

    @Column(name = "activity", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean activity = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_user")
    private UserEntity user;

    public UserBackgroundEntity(UserRequestDto userDto, UserEntity user)
    {
        this.timezone = userDto.timezone();
        if (userDto.notifyEnabled() != null)
            this.notifyEnabled = userDto.notifyEnabled();
        this.user = user;
        user.setBackground(this);
    }
}