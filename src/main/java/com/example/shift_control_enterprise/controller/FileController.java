package com.example.shift_control_enterprise.controller;

import com.example.shift_control_enterprise.service.FileService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/enterprises/{enterpriseId}/files")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/download-data-json")
    public ResponseEntity<byte[]> downloadJson(@PathVariable @NotNull Long enterpriseId,
                                                         @RequestParam @NotNull LocalDate startDate,
                                                         @RequestParam @NotNull LocalDate endDate) {
        byte[] bytes = fileService.makeJsonData(enterpriseId, startDate, endDate);

        String fileName = "data_per_period.json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(bytes.length)
                .body(bytes);

    }

    @GetMapping("/download-data-xlsx")
    public ResponseEntity<byte[]> downloadXlsx(@PathVariable @NotNull Long enterpriseId,
                                                         @RequestParam @NotNull LocalDate startDate,
                                                         @RequestParam @NotNull LocalDate endDate) {
        byte[] bytes = fileService.makeXlsxData(enterpriseId, startDate, endDate);

        String fileName = "data_per_period.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(bytes);
    }

}
