package com.example.shift_control_enterprise.mapper;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee dtoToEmployee(EmployeeDto dto);
}
