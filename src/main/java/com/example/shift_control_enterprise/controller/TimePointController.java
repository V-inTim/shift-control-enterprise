package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.service.TimePointService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprises/time-point")
public class TimePointController {

    private final TimePointService timePointService;
    private static final Logger logger = LoggerFactory.getLogger(TimePointController.class);

    @Autowired
    public TimePointController(TimePointService timePointService) {
        this.timePointService = timePointService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @NotNull Long employeeId){
        logger.info("Получен запрос на создание временной метки.");

        timePointService.makeTimePoint(employeeId);

        logger.info("Отправлен ответ на создание временной метки.");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
