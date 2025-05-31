package com.example.shift_control_enterprise.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
public class WorkShiftHoursDto {
    private LocalDate eventDate;
    private BigDecimal hoursWorked;
    private Long id;
    @JsonIgnore
    private Long employeeId;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private boolean isFinished;

}
