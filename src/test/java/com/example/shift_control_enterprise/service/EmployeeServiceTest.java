package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import com.example.shift_control_enterprise.type.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Long enterpriseId;
    private Long employeeId;
    private Enterprise enterprise;
    private Employee employee;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        enterpriseId = 1L;
        employeeId = 10L;

        enterprise = new Enterprise();
        enterprise.setId(enterpriseId);
        enterprise.setName("Test Enterprise");

        employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setGender(Gender.MALE);
        employee.setEnterprise(enterprise);

        employeeDto = new EmployeeDto();
        employeeDto.setFirstName("John");
        employeeDto.setLastName("Doe");
        employeeDto.setGender(Gender.FEMALE);
    }

    @Test
    void create_ShouldReturnEmployee_WhenEnterpriseExists() {
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));
        when(employeeMapper.dtoToEmployee(any(EmployeeDto.class))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee createdEmployee = employeeService.create(employeeDto, enterpriseId);

        assertNotNull(createdEmployee);
        assertEquals(employeeId, createdEmployee.getId());
        assertEquals(enterprise, createdEmployee.getEnterprise());

        verify(enterpriseRepository, times(1)).findById(enterpriseId);
        verify(employeeMapper, times(1)).dtoToEmployee(employeeDto);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void create_ShouldThrowNoSuchElementException_WhenEnterpriseDoesNotExist() {
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.create(employeeDto, enterpriseId));

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(employeeMapper, never()).dtoToEmployee(any(EmployeeDto.class));
    }

    @Test
    void getById_ShouldReturnEmployee_WhenEmployeeExists() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Employee foundEmployee = employeeService.getById(employeeId, enterpriseId);

        assertNotNull(foundEmployee);
        assertEquals(employeeId, foundEmployee.getId());
        assertEquals("John", foundEmployee.getFirstName());

        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getById_ShouldThrowNoSuchElementException_WhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NoSuchElementException.class, () ->
                employeeService.getById(employeeId, enterpriseId));
        assertEquals("Такого enterprise нет.", exception.getMessage()); // Проверяем сообщение об ошибке
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getAll_ShouldReturnPageOfEmployees() {
        List<Employee> employeeList = Collections.singletonList(employee);
        Page<Employee> employeePage = new PageImpl<>(employeeList);

        when(employeeRepository.findAllByEnterpriseId(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(employeePage);

        Page<Employee> resultPage = employeeService.getAll(enterpriseId, 0, 10);

        assertNotNull(resultPage);
        assertFalse(resultPage.isEmpty());
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(employee, resultPage.getContent().get(0));

        verify(employeeRepository, times(1)).findAllByEnterpriseId(eq(enterpriseId), any(Pageable.class));
    }

    @Test
    void update_ShouldReturnUpdatedEmployee_WhenEnterpriseExists() {
        // Создаем обновленный DTO
        EmployeeDto updatedDto = new EmployeeDto();
        updatedDto.setFirstName("Jane");
        updatedDto.setLastName("Doe");
        updatedDto.setGender(Gender.MALE);

        // Создаем обновленную сущность Employee
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(employeeId);
        updatedEmployee.setFirstName("Jane");
        updatedEmployee.setLastName("Doe");
        updatedEmployee.setGender(Gender.FEMALE);
        updatedEmployee.setEnterprise(enterprise);

        // Устанавливаем поведение моков
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));
        when(employeeMapper.dtoToEmployee(any(EmployeeDto.class))).thenReturn(updatedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Вызываем метод сервиса
        Employee resultEmployee = employeeService.update(employeeId, enterpriseId, updatedDto);

        // Проверяем результаты
        assertNotNull(resultEmployee);
        assertEquals(employeeId, resultEmployee.getId());
        assertEquals("Jane", resultEmployee.getFirstName());
        assertEquals(Gender.FEMALE, resultEmployee.getGender());
        assertEquals(enterprise, resultEmployee.getEnterprise());

        // Проверяем вызовы моков
        verify(enterpriseRepository, times(1)).findById(enterpriseId);
        verify(employeeMapper, times(1)).dtoToEmployee(updatedDto);
        verify(employeeRepository, times(1)).save(updatedEmployee); // Проверяем, что save был вызван с обновленным сотрудником
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenEnterpriseDoesNotExist() {
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                employeeService.update(employeeId, enterpriseId, employeeDto));

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(employeeMapper, never()).dtoToEmployee(any(EmployeeDto.class));
    }


    @Test
    void delete_ShouldCallDeleteById() {
        employeeService.delete(employeeId, enterpriseId);

        verify(employeeRepository, times(1)).deleteById(employeeId);
    }
}

