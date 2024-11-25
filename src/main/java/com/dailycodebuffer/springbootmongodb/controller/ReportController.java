package com.dailycodebuffer.springbootmongodb.controller;

import com.dailycodebuffer.springbootmongodb.dto.*;
import com.dailycodebuffer.springbootmongodb.model.*;
import com.dailycodebuffer.springbootmongodb.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ReportController {
    @Autowired
    private TransactionService transactionService;


    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getTransactionsByMonth(year, month));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(year, month));
    }

    @GetMapping("/type-distribution")
    public ResponseEntity<List<TypeDistributionDTO>> getTypeDistribution(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getTypeDistribution(year, month));
    }
}
