package com.blackrock.retirement.controller;

import com.blackrock.retirement.domain.ParsedTransaction;
import com.blackrock.retirement.dto.ParseTransactionResponse;
import com.blackrock.retirement.dto.TransactionItem;
import com.blackrock.retirement.service.PerformanceMonitorService;
import com.blackrock.retirement.service.TransactionParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for transaction parsing operations
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
public class TransactionController {

    @Autowired
    private TransactionParseService parseService;

    @Autowired
    private PerformanceMonitorService performanceMonitor;

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Parse transactions: calculate ceiling and remanent
     * POST /blackrock/challenge/v1/transactions:parse
     * Request format: [{"date": "2023-10-12 20:15:30", "amount": 250}, ...]
     * Response format: [{"date": "2023-10-12 20:15:30", "amount": 250, "ceiling": 300, "remanent": 50}, ...]
     */
    @PostMapping("transactions:parse")
    public ResponseEntity<List<ParseTransactionResponse>> parse(@RequestBody List<TransactionItem> items) {
        long startTime = System.currentTimeMillis();

        List<ParseTransactionResponse> responses = new ArrayList<>();

        if (items != null && !items.isEmpty()) {
            for (TransactionItem item : items) {
                // Parse date string to timestamp (milliseconds)
                Long timestamp = convertDateToTimestamp(item.getDate());
                Double amount = item.getAmount();
                
                ParsedTransaction parsed = parseService.parse(timestamp, amount);
                
                // Convert to response format
                ParseTransactionResponse response = ParseTransactionResponse.builder()
                        .date(item.getDate())  // Keep original date format
                        .amount(parsed.getOriginalAmount())
                        .ceiling(parsed.getCeiling())
                        .remanent(parsed.getRemanent())
                        .build();
                
                responses.add(response);
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        performanceMonitor.recordExecutionTime(executionTime);

        return ResponseEntity.ok(responses);
    }

    /**
     * Convert date string (yyyy-MM-dd HH:mm:ss) to timestamp in milliseconds
     * Falls back to current time if parsing fails
     */
    private Long convertDateToTimestamp(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateString, DATE_FORMATTER);
            return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            // Fallback to current timestamp if parsing fails
            return System.currentTimeMillis();
        }
    }
}
