package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkShiftsSumPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkTimePerWeekDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatisticService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final HourOperationService hourOperationService;
    private final WorkShiftRepository workShiftRepository;
    private final WebClient webClient;

    public StatisticService(EmployeeRepository employeeRepository,
                            EmployeeMapper employeeMapper,
                            HourOperationService hourOperationService,
                            WorkShiftRepository workShiftRepository,
                            @Value("${external.forecast-service.url}") String forecastUrl) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.hourOperationService = hourOperationService;
        this.workShiftRepository = workShiftRepository;
        this.webClient =  WebClient.create(forecastUrl);
    }

    public Page<WorkShiftsPerPeriodDto> getAllPerWeek(Long enterpriseId, LocalDate date, int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Employee> pageResponse = employeeRepository.findAllByEnterpriseId(enterpriseId, pageRequest);
        List<Long> employeeIds = pageResponse.getContent()
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                date,
                date.plusDays(6)
        );

        return pageResponse.map(employee -> {
            WorkShiftsPerPeriodDto dto = employeeMapper.employeeToWorkShiftsPerWeekDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setWorkShifts(shifts);
            dto.setSumHours(shifts != null ? hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        });
    }

    public Page<WorkShiftsSumPerPeriodDto> getSumPerPeriod(Long enterpriseId, LocalDate start, LocalDate end,
                                                      int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Employee> pageResponse = employeeRepository.findAllByEnterpriseId(enterpriseId, pageRequest);
        List<Long> employeeIds = pageResponse.getContent()
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                start,
                end
        );

        return pageResponse.map(employee -> {
            WorkShiftsSumPerPeriodDto dto = employeeMapper.employeeToWorkShiftsSumPerPeriodDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setSumHours(shifts != null ?  hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        });
    }

    public List<WorkShiftsSumPerPeriodDto> getSumPerPeriodWithLimit(Long enterpriseId, LocalDate start, LocalDate end, int limit){
        List<Employee> employees = employeeRepository.findAllByEnterpriseId(enterpriseId);
        List<Long> employeeIds = employees
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                start,
                end
        );

        return employees.stream().map(employee -> {
            WorkShiftsSumPerPeriodDto dto = employeeMapper.employeeToWorkShiftsSumPerPeriodDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setSumHours(shifts != null ? hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        }).sorted(Comparator.comparing(
                WorkShiftsSumPerPeriodDto::getSumHours,
                Comparator.nullsFirst(Comparator.naturalOrder())
        )).limit(limit).toList();
    }

    public List<WorkTimePerWeekDto> getForecast(Long enterpriseId, int limit){
        LocalDate today = LocalDate.now();
        LocalDate startOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startDateNWeeksAgo = startOfCurrentWeek.minusWeeks(limit - 1);

        BigDecimal secondsInHour = new BigDecimal("3600");

        List<WorkTimePerWeekDto> storedData = workShiftRepository.getWorkTimePerWeeks(
                enterpriseId, startDateNWeeksAgo)
                .stream().map(mt->{
                    BigDecimal secondsBigDecimal = BigDecimal.valueOf(mt.getTotalSeconds());
                    BigDecimal hours = secondsBigDecimal.divide(secondsInHour, 3, RoundingMode.HALF_UP);
                    return new WorkTimePerWeekDto(mt.getYear(), mt.getWeek(), hours, "stored");
                }).toList();

        if (storedData.size() < 5)
            return new ArrayList<WorkTimePerWeekDto>();

        // Отправка в Python-сервис
        List<WorkTimePerWeekDto> predictedData = webClient.post()
                .uri("/forecast")
                .bodyValue(storedData)
                .retrieve()
                .bodyToFlux(WorkTimePerWeekDto.class)
                .collectList()
                .block();

        return Stream.concat(storedData.stream(), predictedData.stream())
                .collect(Collectors.toList());
    }



}
