package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkShiftsSumPerPeriodDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class StatisticService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final HourOperationService hourOperationService;

    public StatisticService(EmployeeRepository employeeRepository,
                            EmployeeMapper employeeMapper,
                            HourOperationService hourOperationService) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.hourOperationService = hourOperationService;
    }

    public Page<WorkShiftsPerPeriodDto> getAllPerWeek(Long enterpriseId, LocalDate date, int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Employee> pageResponse = employeeRepository.findAllByEnterpriseId(enterpriseId, pageRequest);
        List<Long> employeeIds = pageResponse.getContent()
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                date,
                date.plusDays(6)
        );

        return pageResponse.map(employee -> {
            WorkShiftsPerPeriodDto dto = employeeMapper.employeeToWorkShiftsPerWeekDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setWorkShifts(shifts);
            dto.setSumHours(shifts != null ? hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        });
    }

    public Page<WorkShiftsSumPerPeriodDto> getSumPerPeriod(Long enterpriseId, LocalDate start, LocalDate end,
                                                      int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Employee> pageResponse = employeeRepository.findAllByEnterpriseId(enterpriseId, pageRequest);
        List<Long> employeeIds = pageResponse.getContent()
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                start,
                end
        );

        return pageResponse.map(employee -> {
            WorkShiftsSumPerPeriodDto dto = employeeMapper.employeeToWorkShiftsSumPerPeriodDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setSumHours(shifts != null ?  hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        });
    }

    public List<WorkShiftsSumPerPeriodDto> getSumPerPeriodWithLimit(Long enterpriseId, LocalDate start, LocalDate end, int limit){
        List<Employee> employees = employeeRepository.findAllByEnterpriseId(enterpriseId);
        List<Long> employeeIds = employees
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                start,
                end
        );

        return employees.stream().map(employee -> {
            WorkShiftsSumPerPeriodDto dto = employeeMapper.employeeToWorkShiftsSumPerPeriodDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setSumHours(shifts != null ? hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        }).sorted(Comparator.comparing(
                WorkShiftsSumPerPeriodDto::getSumHours,
                Comparator.nullsFirst(Comparator.naturalOrder())
        )).limit(limit).toList();
    }


}
