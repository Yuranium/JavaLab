package com.yuranium.userservice.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    private LocalDateTime dateRegistration;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLogin;

    @Column(name = "timezone", columnDefinition = "VARCHAR(50)", nullable = false)
    private String timezone;

    @Column(name = "activity", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean activity = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_user")
    private UserEntity user;

    public UserBackgroundEntity(String timezone, UserEntity user)
    {
        this.timezone = timezone;
        this.user = user;
        user.setBackground(this);
    }
}