package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, EnterpriseRepository enterpriseRepository,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.employeeMapper = employeeMapper;
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEnterprise(#enterpriseId, authentication)")
    public Employee create(EmployeeDto dto, Long enterpriseId){
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
        Employee employee = employeeMapper.dtoToEmployee(dto);
        System.out.println(employee.getGender());
        employee.setEnterprise(enterprise);
        return employeeRepository.save(employee);
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEmployee(#enterpriseId, #employeeId, authentication)")
    public Employee getById(Long employeeId, Long enterpriseId){
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEnterprise(#enterpriseId, authentication)")
    public List<Employee> getAll(Long enterpriseId){
        return employeeRepository.findAllByEnterpriseId(enterpriseId);
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEmployee(#enterpriseId, #employeeId, authentication)")
    @Transactional
    public Employee update(Long employeeId, Long enterpriseId, EmployeeDto dto){
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
        Employee employee = employeeMapper.dtoToEmployee(dto);
        employee.setEnterprise(enterprise);
        employee.setId(employeeId);
        return employeeRepository.save(employee);
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEmployee(#enterpriseId, #employeeId, authentication)")
    public void delete(Long employeeId, Long enterpriseId){
        employeeRepository.deleteById(employeeId);
    }
}
