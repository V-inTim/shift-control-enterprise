package com.example.shift_control_enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EmployeeDto {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String secondName;
    private LocalDate dateOfBirth;
}
