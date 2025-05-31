package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.EnterpriseDto;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.mapper.EnterpriseMapper;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import com.example.shift_control_enterprise.security.AuthUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseMapper enterpriseMapper;
    private final AuthUtils authUtils;

    @Autowired
    public EnterpriseService(EnterpriseRepository enterpriseRepository, EnterpriseMapper enterpriseMapper,
                             AuthUtils authUtils) {
        this.enterpriseRepository = enterpriseRepository;
        this.enterpriseMapper = enterpriseMapper;
        this.authUtils = authUtils;
    }


    public Enterprise create(EnterpriseDto dto){
        Enterprise enterprise = enterpriseMapper.dtoToEnterprise(dto);
        enterprise.setOwnerId(authUtils.getCurrentUserId());
        return enterpriseRepository.save(enterprise);
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEnterprise(#id, authentication)")
    public Enterprise getById(Long id){
        return enterpriseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Такого enterprise нет."));
    }

    public List<Enterprise> getAll(){
        return enterpriseRepository.findAll();
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEnterprise(#id, authentication)")
    @Transactional
    public Enterprise update(Long id, EnterpriseDto dto){
        if (!enterpriseRepository.existsById(id))
            throw new NoSuchElementException("Такого enterprise нет.");
        Enterprise enterprise = enterpriseMapper.dtoToEnterprise(dto);
        enterprise.setOwnerId(authUtils.getCurrentUserId());
        enterprise.setId(id);
        return enterpriseRepository.save(enterprise);
    }

    @PreAuthorize("@enterprisePermission.hasAccessToEnterprise(#id, authentication)")
    public void delete(Long id){
        enterpriseRepository.deleteById(id);
    }

}
