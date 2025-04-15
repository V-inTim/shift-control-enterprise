package com.example.shift_control_enterprise.repository;

import com.example.shift_control_enterprise.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface EnterpriseRepository extends JpaRepository<Enterprise, UUID> {
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

}
