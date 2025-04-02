package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.EmployeeDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID testId = UUID.randomUUID();
    private final EmployeeDto testDto = new EmployeeDto("John Doe", "john@example.com",
            null, null, UUID.randomUUID());
    private final Enterprise  enterprise = new Enterprise(UUID.randomUUID(), "", null);
    private final Employee testEmployee = new Employee(
            testId, "John Doe", "Fgoo", null, null, enterprise);

    @Test
    void createEmployee_ShouldReturnEmployee() throws Exception {
        Mockito.when(employeeService.create(testDto)).thenReturn(testEmployee);

        mockMvc.perform(post("/enterprise/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John Doe"));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        Mockito.when(employeeService.getById(testId)).thenReturn(testEmployee);

        mockMvc.perform(get("/enterprise/employees/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstName").value("John Doe"));
    }

    @Test
    void getAllEmployees_ShouldReturnList() throws Exception {
        Mockito.when(employeeService.getAll()).thenReturn(Collections.singletonList(testEmployee));

        mockMvc.perform(get("/enterprise/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John Doe"));
    }

    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        Mockito.when(employeeService.update(testId, testDto)).thenReturn(testEmployee);

        mockMvc.perform(put("/enterprise/employees/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstName").value("John Doe"));
    }

    @Test
    void deleteEmployee_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/enterprise/employees/{id}", testId))
                .andExpect(status().isOk());

        Mockito.verify(employeeService).delete(testId);
    }
}

