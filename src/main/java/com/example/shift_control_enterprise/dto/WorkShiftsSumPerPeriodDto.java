package com.example.shift_control_enterprise.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkShiftsSumPerPeriodDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String secondName;
    private BigDecimal sumHours;
}
