package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.service.EmployeeService;
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
@RequestMapping("/enterprises/{enterpriseId}/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Employee> create(@PathVariable Long enterpriseId, @RequestBody @Valid EmployeeDto dto){
        logger.info("Поступил запрос на создание Employee");

        Employee employee = employeeService.create(dto, enterpriseId);

        logger.info("Обработан запрос на создание Employee");
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getById(@PathVariable Long employeeId, @PathVariable Long enterpriseId){
        logger.info("Поступил запрос на получение Employee");

        Employee employee = employeeService.getById(employeeId, enterpriseId);

        logger.info("Обработан запрос на получение Employee");
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAll(@PathVariable Long enterpriseId){
        logger.info("Поступил запрос на получение всех Employee");

        List<Employee> employees = employeeService.getAll(enterpriseId);

        logger.info("Обработан запрос на получение всех Employee");
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<Employee> update(@PathVariable Long employeeId, @PathVariable Long enterpriseId,
                                           @RequestBody @Valid EmployeeDto dto){
        logger.info("Поступил запрос на изменение Employee");

        Employee employee = employeeService.update(employeeId, enterpriseId, dto);

        logger.info("Обработан запрос на изменение Employee");
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> delete(@PathVariable Long employeeId, @PathVariable Long enterpriseId){
        logger.info("Поступил запрос на удаление Employee");

        employeeService.delete(employeeId, enterpriseId);

        logger.info("Обработан запрос на удаление Employee");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
