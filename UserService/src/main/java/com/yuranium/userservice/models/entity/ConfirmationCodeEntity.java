package com.yuranium.userservice.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "confirmation_code")
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmationCodeEntity
{
    @Id
    @Column(name = "id_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_user")
    private Long userId;

    @Column(name = "code")
    private Integer code;
}