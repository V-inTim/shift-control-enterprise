package com.example.shift_control_enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseDto {
    @NotNull
    private String name;
    private String description;
}
