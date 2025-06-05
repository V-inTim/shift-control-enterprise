package com.example.shift_control_enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkTimePerWeekDto {
    private int year;
    private int week;
    private BigDecimal sumHours;
    private String type;
}
