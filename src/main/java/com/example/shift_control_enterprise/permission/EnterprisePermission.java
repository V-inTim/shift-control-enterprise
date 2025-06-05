package com.example.shift_control_enterprise.permission;

import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import com.example.shift_control_enterprise.security.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("enterprisePermission")
public class EnterprisePermission {

    private final EnterpriseRepository enterpriseRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final AuthUtils authUtils;

    @Autowired
    public EnterprisePermission(EnterpriseRepository enterpriseRepository, EmployeeRepository employeeRepository,
                                WorkShiftRepository workShiftRepository,
                                AuthUtils authUtils) {
        this.enterpriseRepository = enterpriseRepository;
        this.employeeRepository = employeeRepository;
        this.workShiftRepository = workShiftRepository;
        this.authUtils = authUtils;
    }

    public boolean hasAccessToEnterprise(Long enterpriseId) {
        UUID userId = authUtils.getCurrentUserId();
        return enterpriseRepository.existsByIdAndOwnerId(enterpriseId, userId);
    }
    public boolean hasAccessToEmployee(Long enterpriseId, Long employeeId) {
        UUID userId = authUtils.getCurrentUserId();
        return enterpriseRepository.existsByIdAndOwnerId(enterpriseId, userId) &&
                employeeRepository.existsByIdAndEnterpriseId(employeeId, enterpriseId);
    }

    public boolean hasAccessToTimePoint(Long employeeId) {
        Long enterpriseId = authUtils.getEnterpriseId();
        return employeeRepository.existsByIdAndEnterpriseId(employeeId, enterpriseId);
    }

    public boolean hasAccessToWorkShift(Long enterpriseId,
                                        Long employeeId,
                                        Long workShiftId) {
        UUID userId = authUtils.getCurrentUserId();
        return enterpriseRepository.existsByIdAndOwnerId(enterpriseId, userId) &&
                employeeRepository.existsByIdAndEnterpriseId(employeeId, enterpriseId) &&
                workShiftRepository.existsByIdAndEmployeeId(workShiftId, employeeId);
    }
}
