package com.example.shift_control_enterprise.mapper;

import com.example.shift_control_enterprise.dto.WorkShiftDto;
import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.entity.WorkShift;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkShiftMapper {
    WorkShift dtoToWorkShift(WorkShiftDto dto);
    WorkShiftHoursDto workShiftToWorkShiftHoursDto(WorkShift workShift);
}
