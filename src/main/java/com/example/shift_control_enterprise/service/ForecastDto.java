package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkTimePerWeekDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForecastDto {
    private List<WorkTimePerWeekDto> storedData;
    private List<WorkTimePerWeekDto> predictedData;
}
