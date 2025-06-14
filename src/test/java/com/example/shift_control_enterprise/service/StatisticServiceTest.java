package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkShiftsSumPerPeriodDto;
import com.example.shift_control_enterprise.dto.WorkTimePerWeekDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StatisticServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private HourOperationService hourOperationService;

    @Mock
    private WorkShiftRepository workShiftRepository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private StatisticService statisticService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        statisticService = new StatisticService(
                employeeRepository,
                employeeMapper,
                hourOperationService,
                workShiftRepository,
                "http://fake-url"
        );
    }

    @Test
    void testGetAllPerWeek() {
        Long enterpriseId = 1L;
        LocalDate date = LocalDate.now();
        int page = 0, size = 1;

        Employee employee = new Employee();
        employee.setId(100L);
        List<Employee> employees = List.of(employee);
        Page<Employee> employeePage = new PageImpl<>(employees);

        when(employeeRepository.findAllByEnterpriseId(eq(enterpriseId), any())).thenReturn(employeePage);
        when(hourOperationService.getHoursMap(any(), eq(date), eq(date.plusDays(6))))
                .thenReturn(Map.of(100L, List.of(new WorkShiftHoursDto())));
        when(employeeMapper.employeeToWorkShiftsPerWeekDto(any()))
                .thenReturn(new WorkShiftsPerPeriodDto());

        Page<WorkShiftsPerPeriodDto> result = statisticService.getAllPerWeek(enterpriseId, date, page, size);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetSumPerPeriod() {
        Long enterpriseId = 1L;
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);

        Employee employee = new Employee();
        employee.setId(101L);
        List<Employee> employees = List.of(employee);
        Page<Employee> employeePage = new PageImpl<>(employees);

        when(employeeRepository.findAllByEnterpriseId(eq(enterpriseId), any())).thenReturn(employeePage);
        when(hourOperationService.getHoursMap(any(), eq(start), eq(end)))
                .thenReturn(Map.of(101L, List.of(new WorkShiftHoursDto())));
        when(employeeMapper.employeeToWorkShiftsSumPerPeriodDto(any()))
                .thenReturn(new WorkShiftsSumPerPeriodDto());

        Page<WorkShiftsSumPerPeriodDto> result = statisticService.getSumPerPeriod(enterpriseId, start, end, 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetForecast_NotEnoughStoredData() {
        Long enterpriseId = 1L;
        int limit = 6;

        when(workShiftRepository.getWorkTimePerWeeks(eq(enterpriseId), any()))
                .thenReturn(List.of()); // менее 5 данных

        List<WorkTimePerWeekDto> result = statisticService.getForecast(enterpriseId, limit);
        assertTrue(result.isEmpty());
    }
}
