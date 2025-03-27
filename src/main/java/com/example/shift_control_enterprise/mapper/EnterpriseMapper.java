package com.example.shift_control_enterprise.mapper;

import com.example.shift_control_enterprise.dto.EnterpriseDto;
import com.example.shift_control_enterprise.entity.Enterprise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EnterpriseMapper {
    Enterprise dtoToEnterprise(EnterpriseDto dto);
}
