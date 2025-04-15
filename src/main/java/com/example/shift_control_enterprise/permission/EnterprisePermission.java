package com.example.shift_control_enterprise.permission;

import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import com.example.shift_control_enterprise.security.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("enterprisePermission")
public class EnterprisePermission {

    private final EnterpriseRepository enterpriseRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthUtils authUtils;

    @Autowired
    public EnterprisePermission(EnterpriseRepository enterpriseRepository, EmployeeRepository employeeRepository,
                                AuthUtils authUtils) {
        this.enterpriseRepository = enterpriseRepository;
        this.employeeRepository = employeeRepository;
        this.authUtils = authUtils;
    }

    public boolean hasAccessToEnterprise(UUID enterpriseId, Authentication authentication) {
        UUID userId = authUtils.getCurrentUserId();
        return enterpriseRepository.existsByIdAndOwnerId(enterpriseId, userId);
    }
    public boolean hasAccessToEmployee(UUID enterpriseId, UUID employeeId, Authentication authentication) {
        UUID userId = authUtils.getCurrentUserId();
        return enterpriseRepository.existsByIdAndOwnerId(enterpriseId, userId) &&
                employeeRepository.existsByIdAndEnterpriseId(employeeId, enterpriseId);
    }
}
