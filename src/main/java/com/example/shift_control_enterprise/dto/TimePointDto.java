package com.example.shift_control_enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TimePointDto {
    @NotNull
    private Long employeeId;
}
