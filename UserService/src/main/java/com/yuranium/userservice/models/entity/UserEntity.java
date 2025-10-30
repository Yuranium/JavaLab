package com.yuranium.userservice.models.entity;

import com.yuranium.userservice.enums.RoleType;
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
@Table(name = "\"user\"")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity
{
    @Id
    @Column(name = "id_user", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private RoleType role = RoleType.USER_ROLE;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "date_registration", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateRegistration;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLogin = LocalDateTime.now();

    @Column(name = "activity")
    private Boolean activity = false;

    @Column(name = "avatar_link")
    private String avatar;
}