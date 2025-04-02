package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Employee create(EmployeeDto dto){
        Enterprise enterprise = enterpriseRepository.findById(dto.getEnterpriseId())
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
        Employee employee = employeeMapper.dtoToEmployee(dto);
        employee.setEnterprise(enterprise);
        return employeeRepository.save(employee);
    }

    public Employee getById(UUID id){
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
    }

    public List<Employee> getAll(){
        return employeeRepository.findAll();
    }

    @Transactional
    public Employee update(UUID id, EmployeeDto dto){
        Enterprise enterprise = enterpriseRepository.findById(dto.getEnterpriseId())
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
        Employee employee = employeeMapper.dtoToEmployee(dto);
        employee.setEnterprise(enterprise);
        employee.setId(id);
        return employeeRepository.save(employee);
    }

    public void delete(UUID id){
        employeeRepository.deleteById(id);
    }
}
