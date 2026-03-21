package com.yuranium.userservice.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

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

    @Column(name = "keycloak_id", nullable = false)
    private UUID keycloakId;

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

    @PreUpdate
    @PrePersist
    public void prePersist()
    {
        if (name != null && name.isBlank())
            name = null;

        if (lastName != null && lastName.isBlank())
            lastName = null;
    }
}