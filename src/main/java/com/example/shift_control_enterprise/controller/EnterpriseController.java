package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.EnterpriseDto;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.service.EnterpriseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/enterprises")
public class EnterpriseController {
    private final EnterpriseService enterpriseService;

    private static final Logger logger = LoggerFactory.getLogger(EnterpriseController.class);

    @Autowired
    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @PostMapping
    public ResponseEntity<Enterprise> create(@RequestBody @Valid EnterpriseDto dto){
        logger.info("Поступил запрос на создание Enterprise");

        Enterprise enterprise = enterpriseService.create(dto);

        logger.info("Обработан запрос на создание Enterprise");
        return new ResponseEntity<>(enterprise, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enterprise> getById(@PathVariable UUID id){
        logger.info("Поступил запрос на получение Enterprise");

        Enterprise enterprise = enterpriseService.getById(id);

        logger.info("Обработан запрос на получение Enterprise");
        return new ResponseEntity<>(enterprise, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Enterprise>> getAll(){
        logger.info("Поступил запрос на получение всех Enterprise");

        List<Enterprise> enterprises = enterpriseService.getAll();

        logger.info("Обработан запрос на получение всех Enterprise");
        return new ResponseEntity<>(enterprises, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enterprise> update(@PathVariable UUID id, @RequestBody @Valid EnterpriseDto dto){
        logger.info("Поступил запрос на изменение Enterprise");

        Enterprise enterprise = enterpriseService.update(id, dto);

        logger.info("Обработан запрос на изменение Enterprise");
        return new ResponseEntity<>(enterprise, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        logger.info("Поступил запрос на удаление Enterprise");

        enterpriseService.delete(id);

        logger.info("Обработан запрос на удаление Enterprise");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
