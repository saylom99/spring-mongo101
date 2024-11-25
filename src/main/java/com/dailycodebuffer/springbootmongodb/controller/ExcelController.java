package com.dailycodebuffer.springbootmongodb.controller;

import com.dailycodebuffer.springbootmongodb.dto.*;
import com.dailycodebuffer.springbootmongodb.model.*;
import com.dailycodebuffer.springbootmongodb.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/excel")
@RestController
public class ExcelController {

    @Autowired
    TransactionService transactionService;
    @Autowired
    ExcelExportService excelExportService;

    @GetMapping(value = "/export/transactions", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam int year,
            @RequestParam int month) {
        List<Transaction> transactions = transactionService.getTransactionsByMonth(year, month);
        byte[] excelContent = excelExportService.exportTransactions(transactions);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions-" + year + "-" + month + ".xlsx")
                .body(excelContent);
    }


    @GetMapping(value = "/export/summary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportSummary(
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> summary = transactionService.getMonthlySummary(year, month);
        byte[] excelContent = excelExportService.exportSummary(summary);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=summary-" + year + "-" + month + ".xlsx")
                .body(excelContent);
    }

    @GetMapping(value = "/export/distribution", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportDistribution(
            @RequestParam int year,
            @RequestParam int month) {
        List<TypeDistributionDTO> distribution = transactionService.getTypeDistribution(year, month);
        byte[] excelContent = excelExportService.exportDistribution(distribution);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=distribution-" + year + "-" + month + ".xlsx")
                .body(excelContent);
    }
}
