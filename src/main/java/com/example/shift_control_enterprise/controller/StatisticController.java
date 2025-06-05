package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkShiftsSumPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkTimePerWeekDto;
import com.example.shift_control_enterprise.service.StatisticService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/enterprises/{enterpriseId}")
public class StatisticController {
    private final StatisticService statisticService;

    private static final Logger logger = LoggerFactory.getLogger(StatisticController.class);

    @Autowired
    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/work-shifts-per-week")
    public ResponseEntity<Page<WorkShiftsPerPeriodDto>> getPerWeek(@PathVariable @NotNull Long enterpriseId,
                                                                   @RequestParam @NotNull LocalDate date,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "50") int size){
        logger.info("Поступил запрос на получение Page<WorkShiftsPerWeekDto>");

        Page<WorkShiftsPerPeriodDto> workShifts = statisticService.getAllPerWeek(enterpriseId, date, page, size);

        logger.info("Обработан запрос на получение Page<WorkShiftsPerWeekDto>");
        return new ResponseEntity<>(workShifts, HttpStatus.OK);
    }

    @GetMapping("/work-shifts-per-period")
    public ResponseEntity<Page<WorkShiftsSumPerPeriodDto>> getPerPeriod(@PathVariable @NotNull Long enterpriseId,
                                                                        @RequestParam @NotNull LocalDate startDate,
                                                                        @RequestParam @NotNull LocalDate endDate,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "50") int size){
        logger.info("Поступил запрос на получение Page<WorkShiftsSumPerPeriodDto>");

        Page<WorkShiftsSumPerPeriodDto> workShifts = statisticService.getSumPerPeriod(enterpriseId, startDate, endDate, page, size);

        logger.info("Обработан запрос на получение Page<WorkShiftsSumPerPeriodDto>");
        return new ResponseEntity<>(workShifts, HttpStatus.OK);
    }

    @GetMapping("/work-shifts-per-period-with-limit")
    public ResponseEntity<List<WorkShiftsSumPerPeriodDto>> getPerPeriodWithLimit(@PathVariable @NotNull Long enterpriseId,
                                                                        @RequestParam @NotNull LocalDate startDate,
                                                                        @RequestParam @NotNull LocalDate endDate,
                                                                        @RequestParam @NotNull int limit){
        logger.info("Поступил запрос /work-shifts-per-period-with-limit");

        List<WorkShiftsSumPerPeriodDto> workShifts = statisticService.getSumPerPeriodWithLimit(enterpriseId, startDate, endDate, limit);

        logger.info("Обработан запрос /work-shifts-per-period-with-limit");
        return new ResponseEntity<>(workShifts, HttpStatus.OK);
    }
    @GetMapping("/work-shifts-forecast")
    public ResponseEntity<List<WorkTimePerWeekDto>> getForecast(@PathVariable @NotNull Long enterpriseId,
                                                   @RequestParam @DefaultValue(value = "50") @Max(value = 200) int limit){
        logger.info("Поступил запрос /enterprises/{enterpriseId}/work-shifts-forecast");

        List<WorkTimePerWeekDto> response = statisticService.getForecast(enterpriseId, limit);

        logger.info("Обработан запрос /enterprises/{enterpriseId}/work-shifts-forecast");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
