package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.exception.EnterpriseException;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ObjectMapper objectMapper; // Мокаем ObjectMapper для контроля JSON сериализации

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private HourOperationService hourOperationService;

    @InjectMocks
    private FileService fileService;

    private Long enterpriseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Employee employee1;
    private Employee employee2;
    private List<Employee> employees;
    private WorkShiftHoursDto shift1_employee1;
    private WorkShiftHoursDto shift2_employee1;
    private WorkShiftHoursDto shift1_employee2;
    private Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee;

    @BeforeEach
    void setUp() {
        enterpriseId = 1L;
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 3); // Период в 3 дня

        // Настройка тестовых данных для сотрудников
        Enterprise enterprise = new Enterprise();
        enterprise.setId(enterpriseId);

        employee1 = new Employee();
        employee1.setId(10L);
        employee1.setFirstName("Ivan");
        employee1.setLastName("Ivanov");
        employee1.setSecondName("Ivanovich");
        employee1.setEnterprise(enterprise);

        employee2 = new Employee();
        employee2.setId(11L);
        employee2.setFirstName("Maria");
        employee2.setLastName("Petrova");
        employee2.setSecondName(null); // Для проверки null значения отчества
        employee2.setEnterprise(enterprise);

        employees = Arrays.asList(employee1, employee2);

        // Настройка тестовых данных для смен
        shift1_employee1 = new WorkShiftHoursDto(LocalDate.of(2023, 1, 1), new BigDecimal("8.00"), 1L, employee1.getId(), LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        shift2_employee1 = new WorkShiftHoursDto(LocalDate.of(2023, 1, 3), new BigDecimal("8.00"), 2L, employee1.getId(), LocalTime.of(10, 0), LocalTime.of(18, 0), true);
        shift1_employee2 = new WorkShiftHoursDto(LocalDate.of(2023, 1, 2), new BigDecimal("8.00"), 3L, employee2.getId(), LocalTime.of(8, 0), LocalTime.of(16, 0), true);

        shiftsByEmployee = new HashMap<>();
        shiftsByEmployee.put(employee1.getId(), Arrays.asList(shift1_employee1, shift2_employee1));
        shiftsByEmployee.put(employee2.getId(), Collections.singletonList(shift1_employee2));

        // Настройка моков по умолчанию
        when(employeeRepository.findAllByEnterpriseId(enterpriseId)).thenReturn(employees);
        when(hourOperationService.getHoursMap(anyList(), eq(startDate), eq(endDate))).thenReturn(shiftsByEmployee);

        // ОБНОВЛЕНО: Mocking EmployeeMapper для использования no-arg конструктора и сеттеров
        // Добавлено lenient()
        lenient().when(employeeMapper.employeeToWorkShiftsPerWeekDto(employee1)).thenAnswer(invocation -> {
            WorkShiftsPerPeriodDto dto = new WorkShiftsPerPeriodDto();
            Employee emp = invocation.getArgument(0);
            dto.setId(emp.getId());
            dto.setFirstName(emp.getFirstName());
            dto.setLastName(emp.getLastName());
            dto.setSecondName(emp.getSecondName());
            return dto;
        });
        // Добавлено lenient()
        lenient().when(employeeMapper.employeeToWorkShiftsPerWeekDto(employee2)).thenAnswer(invocation -> {
            WorkShiftsPerPeriodDto dto = new WorkShiftsPerPeriodDto();
            Employee emp = invocation.getArgument(0);
            dto.setId(emp.getId());
            dto.setFirstName(emp.getFirstName());
            dto.setLastName(emp.getLastName());
            dto.setSecondName(emp.getSecondName());
            return dto;
        });

        // Mocking sumHours for specific shifts lists
        // Добавлено lenient() к конкретным настройкам sumHours
        lenient().when(hourOperationService.sumHours(Arrays.asList(shift1_employee1, shift2_employee1))).thenReturn(new BigDecimal("16.00"));
        lenient().when(hourOperationService.sumHours(Collections.singletonList(shift1_employee2))).thenReturn(new BigDecimal("8.00"));
        lenient().when(hourOperationService.sumHours(eq(null))).thenReturn(new BigDecimal("0")); // Для случаев, когда shiftsByEmployee.get(id) может вернуть null
        // Добавлено lenient() к общей настройке sumHours
        lenient().when(hourOperationService.sumHours(anyList())).thenAnswer((Answer<BigDecimal>) invocation -> {
            List<WorkShiftHoursDto> shifts = invocation.getArgument(0);
            if (shifts == null || shifts.isEmpty()) {
                return new BigDecimal(0);
            }
            return shifts.stream()
                    .map(WorkShiftHoursDto::getHoursWorked)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        });
    }

    // --- makeJsonData Tests ---

    @Test
    void makeJsonData_ShouldReturnJsonBytes_WhenDataIsValid() throws JsonProcessingException {
        byte[] expectedJson = "[]".getBytes();

        when(objectMapper.writeValueAsBytes(anyList())).thenReturn(expectedJson);

        byte[] result = fileService.makeJsonData(enterpriseId, startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedJson, result);
        verify(employeeRepository, times(1)).findAllByEnterpriseId(enterpriseId);
        verify(hourOperationService, times(1)).getHoursMap(anyList(), eq(startDate), eq(endDate));
        verify(employeeMapper, times(2)).employeeToWorkShiftsPerWeekDto(any(Employee.class));
        verify(hourOperationService, times(2)).sumHours(anyList());
        verify(objectMapper, times(1)).writeValueAsBytes(anyList());
    }

    @Test
    void makeJsonData_ShouldThrowEnterpriseException_WhenJsonProcessingFails() throws JsonProcessingException {
        when(objectMapper.writeValueAsBytes(anyList())).thenThrow(new JsonProcessingException("Test JSON error") {});

        EnterpriseException exception = assertThrows(EnterpriseException.class, () ->
                fileService.makeJsonData(enterpriseId, startDate, endDate));
        assertEquals("Ошибка создания json файла.", exception.getMessage());
        verify(objectMapper, times(1)).writeValueAsBytes(anyList());
    }

    // --- makeXlsxData Tests ---

    @Test
    void makeXlsxData_ShouldReturnXlsxBytes_WhenDataIsValid() throws IOException {
        byte[] result = fileService.makeXlsxData(enterpriseId, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(employeeRepository, times(1)).findAllByEnterpriseId(enterpriseId);
        verify(hourOperationService, times(1)).getHoursMap(anyList(), eq(startDate), eq(endDate));
    }

    @Test
    void makeXlsxData_ShouldHandleUnfinishedShiftGracefully() throws IOException {
        WorkShiftHoursDto unfinishedShift = new WorkShiftHoursDto(LocalDate.of(2023, 1, 1), new BigDecimal("0.00"), 4L, employee1.getId(), LocalTime.of(9, 0), null, false);
        shiftsByEmployee.put(employee1.getId(), Collections.singletonList(unfinishedShift));

        when(hourOperationService.sumHours(Collections.singletonList(unfinishedShift))).thenReturn(new BigDecimal("0.00"));

        byte[] result = fileService.makeXlsxData(enterpriseId, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(employeeRepository, times(1)).findAllByEnterpriseId(enterpriseId);
        verify(hourOperationService, times(1)).getHoursMap(anyList(), eq(startDate), eq(endDate));
        verify(hourOperationService, times(1)).sumHours(Collections.singletonList(unfinishedShift));
    }
}
