package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.dto.EnterpriseDto;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.service.EnterpriseService;
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
@WebMvcTest(EnterpriseController.class)
class EnterpriseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnterpriseService enterpriseService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID testId = UUID.randomUUID();
    private final EnterpriseDto testDto = new EnterpriseDto("Test Enterprise", "test@enterprise.com");
    private final Enterprise testEnterprise = new Enterprise(testId, "Test Enterprise", "test@enterprise.com");

    @Test
    void createEnterprise_ShouldReturnEnterprise() throws Exception {
        Mockito.when(enterpriseService.create(testDto)).thenReturn(testEnterprise);

        mockMvc.perform(post("/enterprise/enterprises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.name").value("Test Enterprise"));
    }

    @Test
    void getEnterpriseById_ShouldReturnEnterprise() throws Exception {
        Mockito.when(enterpriseService.getById(testId)).thenReturn(testEnterprise);

        mockMvc.perform(get("/enterprise/enterprises/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.name").value("Test Enterprise"));
    }

    @Test
    void getAllEnterprises_ShouldReturnList() throws Exception {
        Mockito.when(enterpriseService.getAll()).thenReturn(Collections.singletonList(testEnterprise));

        mockMvc.perform(get("/enterprise/enterprises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Enterprise"));
    }

    @Test
    void updateEnterprise_ShouldReturnUpdatedEnterprise() throws Exception {
        Mockito.when(enterpriseService.update(testId, testDto)).thenReturn(testEnterprise);

        mockMvc.perform(put("/enterprise/enterprises/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.name").value("Test Enterprise"));
    }

    @Test
    void deleteEnterprise_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/enterprise/enterprises/{id}", testId))
                .andExpect(status().isOk());

        Mockito.verify(enterpriseService).delete(testId);
    }
}
