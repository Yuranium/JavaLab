package com.yuranium.userservice.models.entity;

import com.yuranium.userservice.enums.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private RoleType role = RoleType.ROLE_USER;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "avatar_link")
    private String avatar;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private UserBackgroundEntity background;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AuthEntity> authMethods = new ArrayList<>();

    @PrePersist
    public void prePersist()
    {
        if (name.isBlank())
            name = null;

        if (lastName.isBlank())
            lastName = null;
    }
}