package com.yuranium.userservice.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "auth")
@NoArgsConstructor
@AllArgsConstructor
public class UserIdempotencyEntity
{
    @Id
    @Column(name = "id_key", columnDefinition = "UUID", unique = true)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDate createdAt;
}