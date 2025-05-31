package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.WorkShiftDto;
import com.example.shift_control_enterprise.entity.WorkShift;
import com.example.shift_control_enterprise.service.WorkShiftService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enterprises/{enterpriseId}/employees/{employeeId}/work-shifts")
public class WorkShiftController {
    private final WorkShiftService workShiftService;

    private static final Logger logger = LoggerFactory.getLogger(WorkShiftController.class);

    @Autowired
    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    @PostMapping
    public ResponseEntity<WorkShift> create(@PathVariable Long enterpriseId,
                                             @PathVariable Long employeeId,
                                             @RequestBody @Valid WorkShiftDto dto){
        logger.info("Поступил запрос на создание WorkShift");

        WorkShift workShift = workShiftService.create(enterpriseId, employeeId, dto);

        logger.info("Обработан запрос на создание WorkShift");
        return new ResponseEntity<>(workShift, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkShift> update(@PathVariable Long enterpriseId,
                                             @PathVariable Long employeeId,
                                             @PathVariable Long id,
                                             @RequestBody @Valid WorkShiftDto dto){
        logger.info("Поступил запрос на изменение WorkShift");

        WorkShift workShift = workShiftService.update(enterpriseId, employeeId, id, dto);

        logger.info("Обработан запрос на изменение WorkShift");
        return new ResponseEntity<>(workShift, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long enterpriseId,
                                       @PathVariable Long employeeId,
                                       @PathVariable Long id){
        logger.info("Поступил запрос на удаление WorkShift");

        workShiftService.delete(enterpriseId, employeeId, id);

        logger.info("Обработан запрос на удаление WorkShift");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
