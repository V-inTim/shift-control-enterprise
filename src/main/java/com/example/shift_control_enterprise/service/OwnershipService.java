package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.OwnershipDto;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import com.example.shift_control_enterprise.security.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OwnershipService {
    private final EnterpriseRepository enterpriseRepository;
    private final AuthUtils authUtils;

    @Autowired
    public OwnershipService(EnterpriseRepository enterpriseRepository, AuthUtils authUtils) {
        this.enterpriseRepository = enterpriseRepository;
        this.authUtils = authUtils;
    }

    public OwnershipDto check(Long enterpriseId){
        UUID userId = authUtils.getCurrentUserId();
        boolean isOwner = enterpriseRepository.existsByIdAndOwnerId(enterpriseId, userId);

        return new OwnershipDto(isOwner);
    }
}
