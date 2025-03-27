package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.EnterpriseDto;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.exception.UserException;
import com.example.shift_control_enterprise.mapper.EnterpriseMapper;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseMapper enterpriseMapper;

    @Autowired
    public EnterpriseService(EnterpriseRepository enterpriseRepository, EnterpriseMapper enterpriseMapper) {
        this.enterpriseRepository = enterpriseRepository;
        this.enterpriseMapper = enterpriseMapper;
    }

    public Enterprise create(EnterpriseDto dto){
        if (dto.getName().isEmpty())
            throw new UserException("Не передано обязательное поле name.");
        return enterpriseRepository.save(enterpriseMapper.dtoToEnterprise(dto));
    }

    public Enterprise getById(UUID id){
        return enterpriseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
    }

    public List<Enterprise> getAll(){
        return enterpriseRepository.findAll();
    }

    @Transactional
    public Enterprise update(UUID id, EnterpriseDto dto){
        return enterpriseRepository.findById(id)
                .map(enterprise -> {
                    Optional.ofNullable(dto.getName()).ifPresent(enterprise::setName);
                    Optional.ofNullable(dto.getDescription()).ifPresent(enterprise::setDescription);
                    return enterpriseRepository.save(enterprise);
                })
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
    }

    public void delete(UUID id){
        enterpriseRepository.deleteById(id);
    }

}
