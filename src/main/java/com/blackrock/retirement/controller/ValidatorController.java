package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.*;
import com.blackrock.retirement.service.PerformanceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for transaction validation operations
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ValidatorController {

    @Autowired
    private PerformanceMonitorService performanceMonitor;

    /**
     * Validate transactions: filter invalid and duplicates
     * POST /blackrock/challenge/v1/transactions:validator
     * 
     * Request format:
     * {
     *   "wage": 50000,
     *   "transactions": [
     *     {"date": "2023-10-12 20:15:30", "amount": 2512, "ceiling": 300.0, "remanent": 50.0},
     *     ...
     *   ]
     * }
     * 
     * Response format:
     * {
     *   "valid": [...],
     *   "invalid": [...]
     * }
     */
    @PostMapping("transactions:validator")
    public ResponseEntity<ValidatorResponse> validate(@RequestBody ValidatorRequest request) {
        long startTime = System.currentTimeMillis();

        List<ValidatorTransactionItem> transactions = request.getTransactions();
        List<ValidatorTransactionResponse> validTransactions = new ArrayList<>();
        List<InvalidTransactionResponse> invalidTransactions = new ArrayList<>();

        if (transactions != null && !transactions.isEmpty()) {
            // Use a Set to track duplicates (date + amount combination)
            Set<String> seenTransactions = new HashSet<>();

            for (ValidatorTransactionItem item : transactions) {
                // Create a unique key for duplicate detection
                String transactionKey = item.getDate() + "|" + item.getAmount();

                // Check for duplicates
                if (seenTransactions.contains(transactionKey)) {
                    InvalidTransactionResponse invalid = InvalidTransactionResponse.builder()
                            .date(item.getDate())
                            .amount(item.getAmount())
                            .ceiling(item.getCeiling())
                            .remanent(item.getRemanent())
                            .message("Duplicate transaction: same date and amount")
                            .build();
                    invalidTransactions.add(invalid);
                } else if (item.getAmount() != null && item.getAmount() < 0) {
                    // Check for negative amounts
                    InvalidTransactionResponse invalid = InvalidTransactionResponse.builder()
                            .date(item.getDate())
                            .amount(item.getAmount())
                            .ceiling(item.getCeiling())
                            .remanent(item.getRemanent())
                            .message("Negative amounts are not allowed")
                            .build();
                    invalidTransactions.add(invalid);
                } else {
                    // Transaction is valid
                    ValidatorTransactionResponse valid = ValidatorTransactionResponse.builder()
                            .date(item.getDate())
                            .amount(item.getAmount())
                            .ceiling(item.getCeiling())
                            .remanent(item.getRemanent())
                            .build();
                    validTransactions.add(valid);
                    seenTransactions.add(transactionKey);
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        performanceMonitor.recordExecutionTime(executionTime);

        ValidatorResponse response = ValidatorResponse.builder()
                .valid(validTransactions)
                .invalid(invalidTransactions)
                .build();

        return ResponseEntity.ok(response);
    }
}
