package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.OwnershipDto;
import com.example.shift_control_enterprise.service.OwnershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/enterprises/{enterpriseId}/check-ownership")
public class OwnershipController {
    private final OwnershipService ownershipService;

    public OwnershipController(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @GetMapping
    public ResponseEntity<OwnershipDto> check(@PathVariable Long enterpriseId){
        OwnershipDto response = ownershipService.check(enterpriseId);
        return ResponseEntity.ok(response);
    }
}
