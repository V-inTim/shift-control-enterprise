package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.EnterpriseDto;
import com.example.shift_control_enterprise.entity.Enterprise;
import com.example.shift_control_enterprise.mapper.EnterpriseMapper;
import com.example.shift_control_enterprise.repository.EnterpriseRepository;
import com.example.shift_control_enterprise.security.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnterpriseServiceTest {

    @Mock // Мокаем EnterpriseRepository
    private EnterpriseRepository enterpriseRepository;

    @Mock // Мокаем EnterpriseMapper
    private EnterpriseMapper enterpriseMapper;

    @Mock // Мокаем AuthUtils
    private AuthUtils authUtils;

    @InjectMocks // Инжектируем моки в EnterpriseService
    private EnterpriseService enterpriseService;

    // Общие данные для тестов
    private Long enterpriseId;
    private UUID currentUserId;
    private Enterprise enterprise;
    private EnterpriseDto enterpriseDto;

    @BeforeEach
    void setUp() {
        enterpriseId = 1L;
        currentUserId = UUID.randomUUID(); // ID текущего пользователя для тестов

        // Создаем тестовую сущность Enterprise
        enterprise = new Enterprise();
        enterprise.setId(enterpriseId);
        enterprise.setName("Test Enterprise");
        enterprise.setOwnerId(currentUserId);

        // Создаем тестовый DTO для Enterprise
        enterpriseDto = new EnterpriseDto();
        enterpriseDto.setName("Test Enterprise");
    }

    @Test
    void create_ShouldReturnNewEnterprise_WithOwnerIdSet() {
        // Устанавливаем поведение моков:
        // Когда enterpriseMapper.dtoToEnterprise вызывается, он должен вернуть enterprise (без ID и ownerId, они будут установлены в сервисе).
        when(enterpriseMapper.dtoToEnterprise(any(EnterpriseDto.class))).thenReturn(new Enterprise()); // Возвращаем новый объект, чтобы ownerId мог быть установлен
        // Когда authUtils.getCurrentUserId() вызывается, он должен вернуть testUser123.
        when(authUtils.getCurrentUserId()).thenReturn(currentUserId);
        // Когда enterpriseRepository.save вызывается с любым Enterprise, он должен вернуть наш employee с уже установленным ID.
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        // Вызываем метод сервиса
        Enterprise createdEnterprise = enterpriseService.create(enterpriseDto);

        // Проверяем результаты:
        assertNotNull(createdEnterprise);
        assertEquals(enterprise.getId(), createdEnterprise.getId());
        assertEquals(currentUserId, createdEnterprise.getOwnerId()); // Убеждаемся, что ownerId установлен

        // Проверяем, что моки были вызваны ожидаемое количество раз:
        verify(enterpriseMapper, times(1)).dtoToEnterprise(enterpriseDto);
        verify(authUtils, times(1)).getCurrentUserId();
        verify(enterpriseRepository, times(1)).save(any(Enterprise.class));
    }

    @Test
    void getById_ShouldReturnEnterprise_WhenEnterpriseExists() {
        // Устанавливаем поведение мока: findById возвращает Optional с enterprise
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.of(enterprise));

        // Вызываем метод сервиса
        Enterprise foundEnterprise = enterpriseService.getById(enterpriseId);

        // Проверяем результаты
        assertNotNull(foundEnterprise);
        assertEquals(enterpriseId, foundEnterprise.getId());
        assertEquals("Test Enterprise", foundEnterprise.getName());

        // Проверяем вызовы моков
        verify(enterpriseRepository, times(1)).findById(enterpriseId);
    }

    @Test
    void getById_ShouldThrowNoSuchElementException_WhenEnterpriseDoesNotExist() {
        // Устанавливаем поведение мока: findById возвращает Optional.empty()
        when(enterpriseRepository.findById(enterpriseId)).thenReturn(Optional.empty());

        // Проверяем, что метод выбрасывает NoSuchElementException
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                enterpriseService.getById(enterpriseId));
        assertEquals("Такого enterprise нет.", exception.getMessage());
        verify(enterpriseRepository, times(1)).findById(enterpriseId);
    }

    @Test
    void getAll_ShouldReturnListOfAllEnterprises() {
        // Создаем список предприятий для возврата
        List<Enterprise> enterpriseList = Collections.singletonList(enterprise);

        // Устанавливаем поведение мока: findAll возвращает созданный список
        when(enterpriseRepository.findAll()).thenReturn(enterpriseList);

        // Вызываем метод сервиса
        List<Enterprise> resultList = enterpriseService.getAll();

        // Проверяем результаты
        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        assertEquals(enterprise, resultList.get(0));

        // Проверяем вызовы моков
        verify(enterpriseRepository, times(1)).findAll();
    }

    @Test
    void update_ShouldReturnUpdatedEnterprise_WhenEnterpriseExists() {
        // Создаем обновленный DTO
        EnterpriseDto updatedDto = new EnterpriseDto();
        updatedDto.setName("Updated Enterprise Name");

        // Создаем обновленную сущность Enterprise
        Enterprise updatedEnterprise = new Enterprise();
        updatedEnterprise.setId(enterpriseId);
        updatedEnterprise.setName("Updated Enterprise Name");
        updatedEnterprise.setOwnerId(currentUserId);

        // Устанавливаем поведение моков
        when(enterpriseRepository.existsById(enterpriseId)).thenReturn(true); // Предприятие существует
        when(enterpriseMapper.dtoToEnterprise(any(EnterpriseDto.class))).thenReturn(new Enterprise()); // Возвращаем новый объект, чтобы ownerId и ID могли быть установлены
        when(authUtils.getCurrentUserId()).thenReturn(currentUserId);
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(updatedEnterprise);

        // Вызываем метод сервиса
        Enterprise resultEnterprise = enterpriseService.update(enterpriseId, updatedDto);

        // Проверяем результаты
        assertNotNull(resultEnterprise);
        assertEquals(enterpriseId, resultEnterprise.getId());
        assertEquals("Updated Enterprise Name", resultEnterprise.getName());
        assertEquals(currentUserId, resultEnterprise.getOwnerId());

        // Проверяем вызовы моков
        verify(enterpriseRepository, times(1)).existsById(enterpriseId);
        verify(enterpriseMapper, times(1)).dtoToEnterprise(updatedDto);
        verify(authUtils, times(1)).getCurrentUserId();
        verify(enterpriseRepository, times(1)).save(any(Enterprise.class));
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenEnterpriseDoesNotExist() {
        // Устанавливаем поведение мока: existsById возвращает false
        when(enterpriseRepository.existsById(enterpriseId)).thenReturn(false);

        // Проверяем, что метод выбрасывает NoSuchElementException
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                enterpriseService.update(enterpriseId, enterpriseDto));
        assertEquals("Такого enterprise нет.", exception.getMessage());

        // Проверяем, что save не был вызван
        verify(enterpriseRepository, never()).save(any(Enterprise.class));
        verify(enterpriseMapper, never()).dtoToEnterprise(any(EnterpriseDto.class));
        verify(authUtils, never()).getCurrentUserId();
    }

    @Test
    void delete_ShouldCallDeleteById() {
        // Вызываем метод сервиса
        enterpriseService.delete(enterpriseId);

        // Проверяем, что deleteById был вызван один раз с правильным ID
        verify(enterpriseRepository, times(1)).deleteById(enterpriseId);
    }
}

