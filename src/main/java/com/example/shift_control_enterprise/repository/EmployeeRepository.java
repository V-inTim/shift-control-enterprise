package com.example.shift_control_enterprise.repository;

import com.example.shift_control_enterprise.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByEnterpriseId(Long enterpriseId);
    Page<Employee> findAllByEnterpriseId(Long enterpriseId, Pageable pageable);
    boolean existsByIdAndEnterpriseId(Long id, Long enterpriseId);
}
