package com.example.shift_control_enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnterpriseDto {
    @NotNull
    private String name;
    private String description;
}
