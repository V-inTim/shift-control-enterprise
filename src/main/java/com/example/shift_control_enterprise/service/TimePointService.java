package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.WorkShift;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class TimePointService {

    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public TimePointService(WorkShiftRepository workShiftRepository, EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }

    @PreAuthorize("@enterprisePermission.hasAccessToTimePoint(#employeeId)")
    public void makeTimePoint(Long employeeId){
        WorkShift workShift;
        Optional<WorkShift> optionalWorkShift = workShiftRepository.findByEmployeeIdAndEventDate(employeeId, LocalDate.now());
        if (optionalWorkShift.isPresent()) {
            workShift = optionalWorkShift.get();
            workShift.setEndTime(LocalTime.now());
        }
        else {
            Employee employee = employeeRepository.getReferenceById(employeeId);
            workShift = WorkShift.builder()
                    .startTime(LocalTime.now())
                    .eventDate(LocalDate.now())
                    .employee(employee)
                    .build();
        }
        workShiftRepository.save(workShift);
    }
}
