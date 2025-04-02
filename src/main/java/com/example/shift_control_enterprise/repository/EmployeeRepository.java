package com.example.shift_control_enterprise.repository;

import com.example.shift_control_enterprise.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
}
