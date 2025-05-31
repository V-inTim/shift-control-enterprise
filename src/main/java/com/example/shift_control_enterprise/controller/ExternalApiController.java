package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkShiftsSumPerPeriodDto;
import com.example.shift_control_enterprise.security.AuthUtils;
import com.example.shift_control_enterprise.service.StatisticService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/enterprises")
public class ExternalApiController {

    private final StatisticService statisticService;
    private final AuthUtils authUtils;

    private static final Logger logger = LoggerFactory.getLogger(StatisticController.class);

    @Autowired
    public ExternalApiController(StatisticService statisticService, AuthUtils authUtils) {
        this.statisticService = statisticService;
        this.authUtils = authUtils;
    }

    @GetMapping("/work-shifts-per-week")
    public ResponseEntity<Page<WorkShiftsPerPeriodDto>> getPerWeek(@RequestParam @NotNull LocalDate date,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "50") int size){
        logger.info("Поступил запрос /api/enterprises/work-shifts-per-week");

        Long enterpriseId = authUtils.getEnterpriseId();
        Page<WorkShiftsPerPeriodDto> workShifts = statisticService.getAllPerWeek(enterpriseId, date, page, size);

        logger.info("Обработан запрос /api/enterprises/work-shifts-per-week");
        return new ResponseEntity<>(workShifts, HttpStatus.OK);
    }

    @GetMapping("/work-shifts-per-period")
    public ResponseEntity<Page<WorkShiftsSumPerPeriodDto>> getPerPeriod(@RequestParam @NotNull LocalDate startDate,
                                                                        @RequestParam @NotNull LocalDate endDate,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "50") int size){
        logger.info("Поступил запрос /api/enterprises/work-shifts-per-period");

        Long enterpriseId = authUtils.getEnterpriseId();
        Page<WorkShiftsSumPerPeriodDto> workShifts = statisticService.getSumPerPeriod(enterpriseId, startDate, endDate, page, size);

        logger.info("Обработан запрос /api/enterprises/work-shifts-per-period");
        return new ResponseEntity<>(workShifts, HttpStatus.OK);
    }
}
