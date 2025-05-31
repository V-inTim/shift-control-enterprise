package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.WorkShift;
import com.example.shift_control_enterprise.mapper.WorkShiftMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class WorkShiftService {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftMapper workShiftMapper;

    @Autowired
    public WorkShiftService(WorkShiftRepository workShiftRepository,
                            EmployeeRepository employeeRepository,
                            WorkShiftMapper workShiftMapper) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
        this.workShiftMapper = workShiftMapper;
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEmployee(#enterpriseId, #employeeId)")
    public WorkShift create (Long enterpriseId, Long employeeId, WorkShiftDto dto){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Такого employee нет."));
        if (workShiftRepository.existsByEmployeeIdAndEventDate(employeeId, dto.getEventDate()))
            throw new DataIntegrityViolationException("Рабочая смена с такой датой уже существует.");
        WorkShift workShift = workShiftMapper.dtoToWorkShift(dto);
        workShift.setEmployee(employee);
        workShift = workShiftRepository.save(workShift);
        return workShift;
    }

    @PreAuthorize("@enterprisePermission.hasAccessToWorkShift(#enterpriseId, #employeeId, #workShiftId)")
    public WorkShift update (Long enterpriseId, Long employeeId, Long workShiftId, WorkShiftDto dto){
        if (!workShiftRepository.existsById(workShiftId))
            throw new NoSuchElementException("Такого work shift нет.");
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Такого employee нет."));
        WorkShift workShift = workShiftMapper.dtoToWorkShift(dto);
        workShift.setEmployee(employee);
        workShift.setId(workShiftId);
        workShift = workShiftRepository.save(workShift);
        return workShift;
    }
    @PreAuthorize("@enterprisePermission.hasAccessToWorkShift(#enterpriseId, #employeeId, #workShiftId)")
    public void delete (Long enterpriseId, Long employeeId, Long workShiftId){
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new NoSuchElementException("Такого work shift нет."));
        workShiftRepository.delete(workShift);
    }
}
