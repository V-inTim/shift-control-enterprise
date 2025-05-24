package com.example.shift_control_enterprise.dto;

import com.example.shift_control_enterprise.type.Gender;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @NotNull
    private Gender gender;
}
