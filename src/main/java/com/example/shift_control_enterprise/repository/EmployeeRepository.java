package com.example.shift_control_enterprise.repository;

import com.example.shift_control_enterprise.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByEnterpriseId(Long enterpriseId);
    boolean existsByIdAndEnterpriseId(Long id, Long enterpriseId);
}
