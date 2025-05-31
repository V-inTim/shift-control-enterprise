package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.mapper.WorkShiftMapper;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HourOperationService {

    private final WorkShiftRepository workShiftRepository;
    private final WorkShiftMapper workShiftMapper;

    @Autowired
    public HourOperationService(WorkShiftRepository workShiftRepository,
                                EmployeeMapper employeeMapper, WorkShiftMapper workShiftMapper) {
        this.workShiftRepository = workShiftRepository;
        this.workShiftMapper = workShiftMapper;
    }

    public Map<Long, List<WorkShiftHoursDto>> getHoursMap(List<Long> employeeIds, LocalDate startDate, LocalDate endDate){
        List<WorkShiftHoursDto> hours = workShiftRepository.findAllByEmployeesAndPeriod(
                        employeeIds,
                        startDate,
                        endDate)
                .stream()
                .map(h -> {
                    WorkShiftHoursDto dto = workShiftMapper.workShiftToWorkShiftHoursDto(h);
                    dto.setEmployeeId(h.getEmployee().getId());
                    dto.setFinished(h.getEndTime() != null);
                    dto.setHoursWorked(h.getEndTime() != null
                            ? calculateHoursBetween(h.getStartTime(), h.getEndTime())
                            : null);
                    return dto;
                }).toList();

        return hours.stream()
                .collect(Collectors.groupingBy(
                        WorkShiftHoursDto::getEmployeeId
                ));
    }

    public BigDecimal sumHours(List<WorkShiftHoursDto> hours) {
        return hours.stream()
                .map(WorkShiftHoursDto::getHoursWorked)
                .filter(Objects::nonNull)  // Фильтруем null значения
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal calculateHoursBetween(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return null;
        }

        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds();

        return BigDecimal.valueOf(seconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

}
