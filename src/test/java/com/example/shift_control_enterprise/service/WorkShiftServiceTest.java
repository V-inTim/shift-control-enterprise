package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.WorkShift;
import com.example.shift_control_enterprise.mapper.WorkShiftMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkShiftServiceTest {

    @Mock
    private WorkShiftRepository workShiftRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkShiftMapper workShiftMapper;

    @InjectMocks
    private WorkShiftService workShiftService;

    private Long enterpriseId;
    private Long employeeId;
    private Long workShiftId;
    private Employee employee;
    private WorkShiftDto workShiftDto;
    private WorkShift workShift;

    @BeforeEach
    void setUp() {
        enterpriseId = 1L;
        employeeId = 10L;
        workShiftId = 100L;

        // Настройка тестовых данных для Employee
        employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstName("John");
        employee.setLastName("Doe");

        // Настройка тестовых данных для WorkShiftDto
        workShiftDto = new WorkShiftDto();
        workShiftDto.setEventDate(LocalDate.of(2023, 1, 1));
        workShiftDto.setStartTime(LocalTime.of(9, 0));
        workShiftDto.setEndTime(LocalTime.of(17, 0));

        // Настройка тестовых данных для WorkShift Entity
        workShift = new WorkShift();
        workShift.setId(workShiftId);
        workShift.setEventDate(LocalDate.of(2023, 1, 1));
        workShift.setStartTime(LocalTime.of(9, 0));
        workShift.setEndTime(LocalTime.of(17, 0));
        workShift.setEmployee(employee);
    }

    // --- Create WorkShift Tests ---

    @Test
    void create_ShouldReturnWorkShift_WhenEmployeeExistsAndNoExistingShift() {
        // Устанавливаем поведение моков
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(workShiftRepository.existsByEmployeeIdAndEventDate(employeeId, workShiftDto.getEventDate())).thenReturn(false);
        when(workShiftMapper.dtoToWorkShift(workShiftDto)).thenReturn(workShift);
        when(workShiftRepository.save(any(WorkShift.class))).thenReturn(workShift);

        // Вызываем тестируемый метод
        WorkShift createdWorkShift = workShiftService.create(enterpriseId, employeeId, workShiftDto);

        // Проверяем результаты
        assertNotNull(createdWorkShift);
        assertEquals(workShiftId, createdWorkShift.getId());
        assertEquals(employee, createdWorkShift.getEmployee());
        assertEquals(workShiftDto.getEventDate(), createdWorkShift.getEventDate());

        // Проверяем вызовы моков
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(workShiftRepository, times(1)).existsByEmployeeIdAndEventDate(employeeId, workShiftDto.getEventDate());
        verify(workShiftMapper, times(1)).dtoToWorkShift(workShiftDto);
        verify(workShiftRepository, times(1)).save(any(WorkShift.class));
    }

    @Test
    void create_ShouldThrowNoSuchElementException_WhenEmployeeDoesNotExist() {
        // Устанавливаем поведение мока: employeeRepository.findById возвращает Optional.empty()
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Проверяем, что метод выбрасывает NoSuchElementException
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                workShiftService.create(enterpriseId, employeeId, workShiftDto));
        assertEquals("Такого employee нет.", exception.getMessage());

        // Проверяем, что другие методы не были вызваны
        verify(workShiftRepository, never()).existsByEmployeeIdAndEventDate(anyLong(), any(LocalDate.class));
        verify(workShiftMapper, never()).dtoToWorkShift(any(WorkShiftDto.class));
        verify(workShiftRepository, never()).save(any(WorkShift.class));
    }

    @Test
    void create_ShouldThrowDataIntegrityViolationException_WhenShiftAlreadyExistsForDate() {
        // Устанавливаем поведение моков
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(workShiftRepository.existsByEmployeeIdAndEventDate(employeeId, workShiftDto.getEventDate())).thenReturn(true); // Смена уже существует

        // Проверяем, что метод выбрасывает DataIntegrityViolationException
        Exception exception = assertThrows(DataIntegrityViolationException.class, () ->
                workShiftService.create(enterpriseId, employeeId, workShiftDto));
        assertEquals("Рабочая смена с такой датой уже существует.", exception.getMessage());

        // Проверяем, что маппер и сохранение не были вызваны
        verify(workShiftMapper, never()).dtoToWorkShift(any(WorkShiftDto.class));
        verify(workShiftRepository, never()).save(any(WorkShift.class));
    }

    // --- Update WorkShift Tests ---

    @Test
    void update_ShouldReturnUpdatedWorkShift_WhenWorkShiftAndEmployeeExist() {
        // Создаем обновленный DTO
        WorkShiftDto updatedDto = new WorkShiftDto();
        updatedDto.setEventDate(LocalDate.of(2023, 1, 2));
        updatedDto.setStartTime(LocalTime.of(10, 0));
        updatedDto.setEndTime(LocalTime.of(18, 0));

        // Создаем обновленную сущность WorkShift (без ID, ID будет установлено в сервисе)
        WorkShift updatedWorkShift = new WorkShift();
        updatedWorkShift.setEventDate(updatedDto.getEventDate());
        updatedWorkShift.setStartTime(updatedDto.getStartTime());
        updatedWorkShift.setEndTime(updatedDto.getEndTime());
        updatedWorkShift.setEmployee(employee);
        updatedWorkShift.setId(workShiftId); // Устанавливаем ID для проверки

        // Устанавливаем поведение моков
        when(workShiftRepository.existsById(workShiftId)).thenReturn(true);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(workShiftMapper.dtoToWorkShift(updatedDto)).thenReturn(updatedWorkShift);
        when(workShiftRepository.save(any(WorkShift.class))).thenReturn(updatedWorkShift);

        // Вызываем тестируемый метод
        WorkShift result = workShiftService.update(enterpriseId, employeeId, workShiftId, updatedDto);

        // Проверяем результаты
        assertNotNull(result);
        assertEquals(workShiftId, result.getId());
        assertEquals(updatedDto.getEventDate(), result.getEventDate());
        assertEquals(updatedDto.getStartTime(), result.getStartTime());
        assertEquals(updatedDto.getEndTime(), result.getEndTime());
        assertEquals(employee, result.getEmployee());

        // Проверяем вызовы моков
        verify(workShiftRepository, times(1)).existsById(workShiftId);
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(workShiftMapper, times(1)).dtoToWorkShift(updatedDto);
        verify(workShiftRepository, times(1)).save(any(WorkShift.class));
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenWorkShiftDoesNotExist() {
        // Устанавливаем поведение мока: workShiftRepository.existsById возвращает false
        when(workShiftRepository.existsById(workShiftId)).thenReturn(false);

        // Проверяем, что метод выбрасывает NoSuchElementException
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                workShiftService.update(enterpriseId, employeeId, workShiftId, workShiftDto));
        assertEquals("Такого work shift нет.", exception.getMessage());

        // Проверяем, что другие методы не были вызваны
        verify(employeeRepository, never()).findById(anyLong());
        verify(workShiftMapper, never()).dtoToWorkShift(any(WorkShiftDto.class));
        verify(workShiftRepository, never()).save(any(WorkShift.class));
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenEmployeeDoesNotExist() {
        // Устанавливаем поведение моков
        when(workShiftRepository.existsById(workShiftId)).thenReturn(true);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty()); // Сотрудник не найден

        // Проверяем, что метод выбрасывает NoSuchElementException
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                workShiftService.update(enterpriseId, employeeId, workShiftId, workShiftDto));
        assertEquals("Такого employee нет.", exception.getMessage());

        // Проверяем, что маппер и сохранение не были вызваны
        verify(workShiftMapper, never()).dtoToWorkShift(any(WorkShiftDto.class));
        verify(workShiftRepository, never()).save(any(WorkShift.class));
    }

    // --- Delete WorkShift Tests ---

    @Test
    void delete_ShouldCallDelete_WhenWorkShiftExists() {
        // Устанавливаем поведение мока: findById возвращает Optional с workShift
        when(workShiftRepository.findById(workShiftId)).thenReturn(Optional.of(workShift));
        // doNothing() по умолчанию для void методов, но можно явно указать
        doNothing().when(workShiftRepository).delete(workShift);

        // Вызываем тестируемый метод
        workShiftService.delete(enterpriseId, employeeId, workShiftId);

        // Проверяем вызовы моков
        verify(workShiftRepository, times(1)).findById(workShiftId);
        verify(workShiftRepository, times(1)).delete(workShift);
    }

    @Test
    void delete_ShouldThrowNoSuchElementException_WhenWorkShiftDoesNotExist() {
        // Устанавливаем поведение мока: findById возвращает Optional.empty()
        when(workShiftRepository.findById(workShiftId)).thenReturn(Optional.empty());

        // Проверяем, что метод выбрасывает NoSuchElementException
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                workShiftService.delete(enterpriseId, employeeId, workShiftId));
        assertEquals("Такого work shift нет.", exception.getMessage());

        // Проверяем, что delete не был вызван
        verify(workShiftRepository, never()).delete(any(WorkShift.class));
    }
}

