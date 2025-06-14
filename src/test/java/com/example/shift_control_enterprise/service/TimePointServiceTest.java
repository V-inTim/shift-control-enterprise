package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.WorkShift;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimePointServiceTest {

    @Mock
    private WorkShiftRepository workShiftRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TimePointService timePointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMakeTimePoint_updateEndTime_whenShiftExists() {
        Long employeeId = 1L;
        LocalDate today = LocalDate.now();

        WorkShift existingShift = WorkShift.builder()
                .id(10L)
                .employee(new Employee())
                .eventDate(today)
                .startTime(LocalTime.of(9, 0))
                .build();

        when(workShiftRepository.findByEmployeeIdAndEventDate(employeeId, today))
                .thenReturn(Optional.of(existingShift));

        timePointService.makeTimePoint(employeeId);

        assertNotNull(existingShift.getEndTime(), "endTime должно быть установлено");
        verify(workShiftRepository).save(existingShift);
        verify(employeeRepository, never()).getReferenceById(any());
    }

    @Test
    void testMakeTimePoint_createNewShift_whenNoShiftExists() {
        Long employeeId = 2L;
        LocalDate today = LocalDate.now();
        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);

        when(workShiftRepository.findByEmployeeIdAndEventDate(employeeId, today))
                .thenReturn(Optional.empty());
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(mockEmployee);

        ArgumentCaptor<WorkShift> captor = ArgumentCaptor.forClass(WorkShift.class);

        timePointService.makeTimePoint(employeeId);

        verify(workShiftRepository).save(captor.capture());
        WorkShift savedShift = captor.getValue();

        assertEquals(today, savedShift.getEventDate());
        assertNotNull(savedShift.getStartTime());
        assertEquals(mockEmployee, savedShift.getEmployee());
        assertNull(savedShift.getEndTime()); // Убедимся, что не устанавливается
    }
}

