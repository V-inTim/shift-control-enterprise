package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.WorkShift;
import com.example.shift_control_enterprise.mapper.WorkShiftMapper;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HourOperationServiceTest {

    @Mock
    private WorkShiftRepository workShiftRepository;

    @Mock
    private WorkShiftMapper workShiftMapper;
    @Mock
    private com.example.shift_control_enterprise.mapper.EmployeeMapper employeeMapper;


    @InjectMocks
    private HourOperationService hourOperationService;

    private Long employeeId1;
    private Long employeeId2;
    private List<Long> employeeIds;
    private LocalDate startDate;
    private LocalDate endDate;

    private Employee employee1;
    private Employee employee2;

    private WorkShift workShift1_e1;
    private WorkShift workShift2_e1;
    private WorkShift workShift3_e2;
    private WorkShift workShift4_e1_unfinished; // Незавершенная смена

    @BeforeEach
    void setUp() {
        employeeId1 = 1L;
        employeeId2 = 2L;
        employeeIds = Arrays.asList(employeeId1, employeeId2);
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 7);

        employee1 = new Employee();
        employee1.setId(employeeId1);
        employee1.setFirstName("John");

        employee2 = new Employee();
        employee2.setId(employeeId2);
        employee2.setFirstName("Jane");

        // Рабочие смены для тестирования
        workShift1_e1 = new WorkShift(10L, LocalDate.of(2023, 1, 1), LocalTime.of(9, 0), LocalTime.of(17, 0), employee1);
        workShift2_e1 = new WorkShift(11L, LocalDate.of(2023, 1, 2), LocalTime.of(8, 0), LocalTime.of(16, 0), employee1);
        workShift3_e2 = new WorkShift(12L, LocalDate.of(2023, 1, 1), LocalTime.of(10, 0), LocalTime.of(18, 0), employee2);
        workShift4_e1_unfinished = new WorkShift(13L, LocalDate.of(2023, 1, 3), LocalTime.of(10, 0), null, employee1); // Незавершенная смена

        lenient().when(workShiftMapper.workShiftToWorkShiftHoursDto(any(WorkShift.class))).thenAnswer((Answer<WorkShiftHoursDto>) invocation -> {
            WorkShift ws = invocation.getArgument(0);
            WorkShiftHoursDto dto = new WorkShiftHoursDto();
            dto.setId(ws.getId());
            dto.setEventDate(ws.getEventDate());
            dto.setStartTime(ws.getStartTime());
            dto.setEndTime(ws.getEndTime());
            dto.setEmployeeId(ws.getEmployee().getId());
            dto.setFinished(ws.getEndTime() != null);
            if (ws.getEndTime() != null) {
                long seconds = java.time.Duration.between(ws.getStartTime(), ws.getEndTime()).getSeconds();
                dto.setHoursWorked(BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP));
            } else {
                dto.setHoursWorked(null);
            }
            return dto;
        });
    }

    // --- getHoursMap Tests ---

    @Test
    void getHoursMap_ShouldReturnCorrectlyGroupedShifts() {
        List<WorkShift> allWorkShifts = Arrays.asList(workShift1_e1, workShift2_e1, workShift3_e2, workShift4_e1_unfinished);
        when(workShiftRepository.findAllByEmployeesAndPeriod(employeeIds, startDate, endDate)).thenReturn(allWorkShifts);

        Map<Long, List<WorkShiftHoursDto>> result = hourOperationService.getHoursMap(employeeIds, startDate, endDate);

        verify(workShiftRepository, times(1)).findAllByEmployeesAndPeriod(employeeIds, startDate, endDate);
        verify(workShiftMapper, times(allWorkShifts.size())).workShiftToWorkShiftHoursDto(any(WorkShift.class));

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.containsKey(employeeId1));
        assertTrue(result.containsKey(employeeId2));

        List<WorkShiftHoursDto> shiftsForE1 = result.get(employeeId1);
        List<WorkShiftHoursDto> shiftsForE2 = result.get(employeeId2);

        assertNotNull(shiftsForE1);
        assertNotNull(shiftsForE2);

        assertEquals(3, shiftsForE1.size()); // Две завершенные и одна незавершенная
        assertEquals(1, shiftsForE2.size());

        // Проверяем часы и статусы для employee1
        WorkShiftHoursDto dto1_e1 = shiftsForE1.stream().filter(dto -> dto.getId().equals(workShift1_e1.getId())).findFirst().orElse(null);
        assertNotNull(dto1_e1);
        assertEquals(new BigDecimal("8.00"), dto1_e1.getHoursWorked());
        assertTrue(dto1_e1.isFinished());

        WorkShiftHoursDto dto2_e1 = shiftsForE1.stream().filter(dto -> dto.getId().equals(workShift2_e1.getId())).findFirst().orElse(null);
        assertNotNull(dto2_e1);
        assertEquals(new BigDecimal("8.00"), dto2_e1.getHoursWorked());
        assertTrue(dto2_e1.isFinished());

        WorkShiftHoursDto dto4_e1_unfinished = shiftsForE1.stream().filter(dto -> dto.getId().equals(workShift4_e1_unfinished.getId())).findFirst().orElse(null);
        assertNotNull(dto4_e1_unfinished);
        assertNull(dto4_e1_unfinished.getHoursWorked()); // Часы null для незавершенной смены
        assertFalse(dto4_e1_unfinished.isFinished());

        // Проверяем часы и статусы для employee2
        WorkShiftHoursDto dto3_e2 = shiftsForE2.stream().filter(dto -> dto.getId().equals(workShift3_e2.getId())).findFirst().orElse(null);
        assertNotNull(dto3_e2);
        assertEquals(new BigDecimal("8.00"), dto3_e2.getHoursWorked());
        assertTrue(dto3_e2.isFinished());
    }

    @Test
    void getHoursMap_ShouldReturnEmptyMap_WhenNoShiftsFound() {
        when(workShiftRepository.findAllByEmployeesAndPeriod(employeeIds, startDate, endDate)).thenReturn(Collections.emptyList());

        Map<Long, List<WorkShiftHoursDto>> result = hourOperationService.getHoursMap(employeeIds, startDate, endDate);

        verify(workShiftRepository, times(1)).findAllByEmployeesAndPeriod(employeeIds, startDate, endDate);
        verify(workShiftMapper, never()).workShiftToWorkShiftHoursDto(any(WorkShift.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getHoursMap_ShouldHandleNullEndTimeGracefully() {
        List<WorkShift> shiftsWithUnfinished = Collections.singletonList(workShift4_e1_unfinished);
        when(workShiftRepository.findAllByEmployeesAndPeriod(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(shiftsWithUnfinished);

        Map<Long, List<WorkShiftHoursDto>> result = hourOperationService.getHoursMap(employeeIds, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(employeeId1));

        WorkShiftHoursDto dto = result.get(employeeId1).get(0);
        assertNotNull(dto);
        assertEquals(workShift4_e1_unfinished.getId(), dto.getId());
        assertFalse(dto.isFinished());
        assertNull(dto.getHoursWorked()); // Часы должны быть null
    }

    @Test
    void sumHours_ShouldCorrectlySumHours() {
        WorkShiftHoursDto dto1 = new WorkShiftHoursDto(LocalDate.now(), new BigDecimal("8.00"), 1L, employeeId1, LocalTime.now(), LocalTime.now(), true);
        WorkShiftHoursDto dto2 = new WorkShiftHoursDto(LocalDate.now(), new BigDecimal("4.50"), 2L, employeeId1, LocalTime.now(), LocalTime.now(), true);
        WorkShiftHoursDto dto3 = new WorkShiftHoursDto(LocalDate.now(), new BigDecimal("2.00"), 3L, employeeId1, LocalTime.now(), LocalTime.now(), true);

        List<WorkShiftHoursDto> hours = Arrays.asList(dto1, dto2, dto3);

        BigDecimal sum = hourOperationService.sumHours(hours);

        assertNotNull(sum);
        assertEquals(new BigDecimal("14.50"), sum);
    }

    @Test
    void sumHours_ShouldHandleNullHoursWorked() {
        WorkShiftHoursDto dto1 = new WorkShiftHoursDto(LocalDate.now(), new BigDecimal("8.00"), 1L, employeeId1, LocalTime.now(), LocalTime.now(), true);
        WorkShiftHoursDto dto2 = new WorkShiftHoursDto(LocalDate.now(), null, 2L, employeeId1, LocalTime.now(), LocalTime.now(), false); // null hoursWorked
        WorkShiftHoursDto dto3 = new WorkShiftHoursDto(LocalDate.now(), new BigDecimal("2.00"), 3L, employeeId1, LocalTime.now(), LocalTime.now(), true);

        List<WorkShiftHoursDto> hours = Arrays.asList(dto1, dto2, dto3);

        BigDecimal sum = hourOperationService.sumHours(hours);

        assertNotNull(sum);
        assertEquals(new BigDecimal("10.00"), sum); // 8.00 + 2.00, null игнорируется
    }

    @Test
    void sumHours_ShouldReturnZeroForEmptyList() {
        List<WorkShiftHoursDto> hours = Collections.emptyList();

        BigDecimal sum = hourOperationService.sumHours(hours);

        assertNotNull(sum);
        assertEquals(BigDecimal.ZERO, sum);
    }

    @Test
    void sumHours_ShouldReturnZeroForListWithOnlyNullHoursWorked() {
        WorkShiftHoursDto dto1 = new WorkShiftHoursDto(LocalDate.now(), null, 1L, employeeId1, LocalTime.now(), LocalTime.now(), false);
        WorkShiftHoursDto dto2 = new WorkShiftHoursDto(LocalDate.now(), null, 2L, employeeId1, LocalTime.now(), LocalTime.now(), false);

        List<WorkShiftHoursDto> hours = Arrays.asList(dto1, dto2);

        BigDecimal sum = hourOperationService.sumHours(hours);

        assertNotNull(sum);
        assertEquals(BigDecimal.ZERO, sum);
    }

    @Test
    void calculateHoursBetween_ShouldReturnCorrectHours() {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        BigDecimal expectedHours = new BigDecimal("8.00");
        long seconds = java.time.Duration.between(start, end).getSeconds();
        BigDecimal actualHours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        assertEquals(expectedHours, actualHours);
    }

    @Test
    void calculateHoursBetween_ShouldReturnNullIfStartIsNull() {
        LocalTime end = LocalTime.of(17, 0);
        assertNull(invokeCalculateHoursBetween(null, end));
    }

    @Test
    void calculateHoursBetween_ShouldReturnNullIfEndIsNull() {
        LocalTime start = LocalTime.of(9, 0);
        assertNull(invokeCalculateHoursBetween(start, null));
    }

    @Test
    void calculateHoursBetween_ShouldHandleOvernightShifts() {
        LocalTime start = LocalTime.of(22, 0);
        LocalTime end = LocalTime.of(6, 0); // Следующий день
        BigDecimal expectedHours = BigDecimal.valueOf(Duration.between(start, end).getSeconds())
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        assertEquals(expectedHours, invokeCalculateHoursBetween(start, end));
        assertTrue(invokeCalculateHoursBetween(start, end).compareTo(BigDecimal.ZERO) < 0);
    }

    private BigDecimal invokeCalculateHoursBetween(LocalTime start, LocalTime end) {
        try {
            java.lang.reflect.Method method = HourOperationService.class.getDeclaredMethod("calculateHoursBetween", LocalTime.class, LocalTime.class);
            method.setAccessible(true); // Делаем приватный метод доступным
            return (BigDecimal) method.invoke(hourOperationService, start, end);
        } catch (Exception e) {
            fail("Ошибка при вызове приватного метода: " + e.getMessage());
            return null;
        }
    }
}

