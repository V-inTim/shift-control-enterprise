package com.example.shift_control_enterprise.mapper;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    @Mapping(source = "dto.gender", target = "gender")
    Employee dtoToEmployee(EmployeeDto dto);
}
