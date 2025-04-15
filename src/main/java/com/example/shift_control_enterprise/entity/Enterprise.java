package com.example.shift_control_enterprise.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "enterprises")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    @Column(name = "owner_id")
    private UUID ownerId;
}
