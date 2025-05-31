package com.example.shift_control_enterprise.service;

import com.example.shift_control_enterprise.dto.WorkShiftHoursDto;
import com.example.shift_control_enterprise.dto.WorkShiftsPerPeriodDto;
import com.example.shift_control_enterprise.entity.Employee;
import com.example.shift_control_enterprise.exception.EnterpriseException;
import com.example.shift_control_enterprise.mapper.EmployeeMapper;
import com.example.shift_control_enterprise.repository.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class FileService {
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;
    private final EmployeeMapper employeeMapper;
    private final HourOperationService hourOperationService;

    @Autowired
    public FileService(EmployeeRepository employeeRepository, ObjectMapper objectMapper,
                       EmployeeMapper employeeMapper, HourOperationService hourOperationService) {
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
        this.employeeMapper = employeeMapper;
        this.hourOperationService = hourOperationService;
    }

    public byte[] makeJsonData(Long enterpriseId, LocalDate startDate, LocalDate endDate){
        List<Employee> employees = employeeRepository.findAllByEnterpriseId(enterpriseId);
        List<Long> employeeIds = employees
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                startDate,
                endDate
        );

        List<WorkShiftsPerPeriodDto> data = employees.stream().map(employee -> {
            WorkShiftsPerPeriodDto dto = employeeMapper.employeeToWorkShiftsPerWeekDto(employee);
            List<WorkShiftHoursDto> shifts = shiftsByEmployee.get(employee.getId());
            dto.setWorkShifts(shifts);
            dto.setSumHours(shifts != null ? hourOperationService.sumHours(shifts) : new BigDecimal(0));
            return dto;
        }).toList();


        byte[] jsonBytes;

        try {
            jsonBytes = objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new EnterpriseException("Ошибка создания json файла.");
        }

        return jsonBytes;
    }

    public byte[] makeXlsxData(Long enterpriseId, LocalDate startDate, LocalDate endDate){
        List<Employee> employees = employeeRepository.findAllByEnterpriseId(enterpriseId);
        List<Long> employeeIds = employees
                .stream()
                .map(Employee::getId)
                .toList();

        Map<Long, List<WorkShiftHoursDto>> shiftsByEmployee = hourOperationService.getHoursMap(
                employeeIds,
                startDate,
                endDate
        );

        Workbook workbook = new XSSFWorkbook();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        int sumIndex = (int)ChronoUnit.DAYS.between(startDate, endDate) + 1;

        Sheet sheet = workbook.createSheet("Данные о рабочих часах");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        Cell cell = headerRow.createCell(0);
        cell.setCellValue("Сотрудники");
        cell.setCellStyle(headerStyle);

        int columnIndex = 1;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            cell = headerRow.createCell(columnIndex);
            cell.setCellValue(date.format(dateFormatter));
            cell.setCellStyle(headerStyle);
            columnIndex += 1;
        }
        headerRow.createCell(columnIndex).setCellValue("Сумма часов");

        // заполнение ячеек
        int rowIndex = 1;
        for (Employee employee: employees){

            Row row1 = sheet.createRow(rowIndex);
            Row row2 = sheet.createRow(rowIndex + 1);
            String fullName = String.format(
                    "%s %s %s",
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getSecondName() == null ? "" : employee.getSecondName());
            row1.createCell(0).setCellValue(fullName);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 0, 0));
            columnIndex = 1;

            List<WorkShiftHoursDto> hours = shiftsByEmployee.get(employee.getId());
            if (hours != null && !hours.isEmpty()){
                Iterator<WorkShiftHoursDto> iterator = hours.iterator();
                WorkShiftHoursDto dto = iterator.next();
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if (dto.getEventDate().equals(date)){
                        row1.createCell(columnIndex).setCellValue(
                                dto.isFinished() ? dto.getHoursWorked().doubleValue() : 0.0);
                        row2.createCell(columnIndex).setCellValue(
                                String.format(
                                        "%s - %s",
                                        dto.getStartTime().format(timeFormatter),
                                        dto.isFinished() ? dto.getEndTime().format(timeFormatter) : "не завершено"
                                )
                        );
                        if (iterator.hasNext())
                            dto = iterator.next();
                        else
                            break;
                    }
                    columnIndex += 1;
                }

            }
            row1.createCell(sumIndex + 1).setCellValue(hours != null ? hourOperationService.sumHours(hours).doubleValue() : 0.0);

            rowIndex += 2;
        }

        for (int i = 0; i < sumIndex + 1; i++)
            sheet.autoSizeColumn(i);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new EnterpriseException("Ошибка создания xslx файла.");
        }
        return  outputStream.toByteArray();
    }
}
