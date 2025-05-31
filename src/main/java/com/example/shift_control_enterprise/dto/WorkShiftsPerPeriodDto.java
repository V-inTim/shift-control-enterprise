package com.example.shift_control_enterprise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WorkShiftsPerPeriodDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String secondName;
    private List<WorkShiftHoursDto> workShifts;
    private BigDecimal sumHours;
}
