package com.blackrock.retirement.controller;

import com.blackrock.retirement.domain.FilteredTransaction;
import com.blackrock.retirement.dto.*;
import com.blackrock.retirement.service.PerformanceMonitorService;
import com.blackrock.retirement.service.TemporalFilterService;
import com.blackrock.retirement.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for temporal filtering operations
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
@RequiredArgsConstructor
public class FilterController {

    private final TemporalFilterService filterService;
    private final PerformanceMonitorService performanceMonitor;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Apply temporal rules (q, p, k) to transactions
     * POST /blackrock/challenge/v1/transactions:filter
     */
    @PostMapping("transactions:filter")
    public ResponseEntity<FilterResponse> filter(@RequestBody FilterRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Validate request
            if (request == null || request.getTransactions() == null || request.getTransactions().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            
            // Step 1: Identify invalid transactions and build valid transaction list
            List<FilterTransactionItem> validItems = new ArrayList<>();
            List<FilterInvalidTransactionResponse> invalidTransactions = new ArrayList<>();
            Map<String, Boolean> seenKeys = new HashMap<>();  // Track (date + amount) combinations
            
            for (FilterTransactionItem item : request.getTransactions()) {
                String key = item.getDate() + "|" + item.getAmount();
                Double amount = item.getAmount();
                
                // Check for negative amounts
                if (amount != null && amount < 0) {
                    Double ceiling = item.getCeiling();
                    Double remanent = item.getRemanent();
                    
                    if (ceiling == null && amount > 0) {
                        ceiling = Math.ceil(amount / 100.0) * 100.0;
                    }
                    if (remanent == null && ceiling != null) {
                        remanent = ceiling - amount;
                    }
                    
                    FilterInvalidTransactionResponse invalidResponse = FilterInvalidTransactionResponse.builder()
                            .date(item.getDate())
                            .amount(amount)
                            .message("Negative amounts are not allowed")
                            .build();
                    invalidTransactions.add(invalidResponse);
                }
                // Check for duplicates
                else if (seenKeys.containsKey(key)) {
                    Double ceiling = item.getCeiling();
                    Double remanent = item.getRemanent();
                    
                    if (ceiling == null && amount != null && amount > 0) {
                        ceiling = Math.ceil(amount / 100.0) * 100.0;
                    }
                    if (remanent == null && ceiling != null && amount != null) {
                        remanent = ceiling - amount;
                    }
                    
                    FilterInvalidTransactionResponse invalidResponse = FilterInvalidTransactionResponse.builder()
                            .date(item.getDate())
                            .amount(amount)
                            .message("Duplicate transaction: same date and amount")
                            .build();
                    invalidTransactions.add(invalidResponse);
                }
                // Valid transaction
                else {
                    seenKeys.put(key, true);
                    validItems.add(item);
                }
            }
            
            // Step 2: Convert valid FilterTransactionItems to ParsedTransactions
            List<com.blackrock.retirement.domain.ParsedTransaction> validTransactions = 
                validItems.stream().map(item -> {
                    try {
                        Date date = dateFormat.parse(item.getDate());
                        
                        // Calculate ceiling and remanent if not provided
                        Double amount = item.getAmount();
                        Double ceiling = item.getCeiling();
                        Double remanent = item.getRemanent();
                        
                        if (ceiling == null && amount != null && amount > 0) {
                            ceiling = Math.ceil(amount / 100.0) * 100.0;
                        }
                        if (remanent == null && ceiling != null && amount != null) {
                            remanent = ceiling - amount;
                        }
                        
                        return com.blackrock.retirement.domain.ParsedTransaction.builder()
                                .timestamp(date.getTime())
                                .originalAmount(amount)
                                .ceiling(ceiling)
                                .remanent(remanent)
                                .valid(true)
                                .build();
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format: " + item.getDate(), e);
                    }
                }).collect(Collectors.toList());

            // Step 3: Convert period DTOs to TemporalPeriods
            List<com.blackrock.retirement.domain.TemporalPeriod> qPeriods = new ArrayList<>();
            if (request.getQ() != null) {
                for (FilterPeriodQ q : request.getQ()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                        long startDate = sdf.parse(q.getStart()).getTime();
                        long endDate = sdf.parse(q.getEnd()).getTime();
                        qPeriods.add(com.blackrock.retirement.domain.TemporalPeriod.builder()
                                .startDate(startDate)
                                .endDate(endDate)
                                .amount(q.getFixed())
                                .periodType("q")
                                .build());
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format in q period", e);
                    }
                }
            }
            
            // Step 3.5: Filter out transactions that fall within Q periods
            List<com.blackrock.retirement.domain.ParsedTransaction> transactionsToExclude = new ArrayList<>();
            for (com.blackrock.retirement.domain.ParsedTransaction tx : validTransactions) {
                boolean inQPeriod = false;
                for (com.blackrock.retirement.domain.TemporalPeriod qPeriod : qPeriods) {
                    if (tx.getTimestamp() >= qPeriod.getStartDate() && tx.getTimestamp() <= qPeriod.getEndDate()) {
                        inQPeriod = true;
                        break;
                    }
                }
                if (inQPeriod) {
                    transactionsToExclude.add(tx);
                }
            }
            
            // Remove transactions in Q periods from valid list
            validTransactions.removeAll(transactionsToExclude);
            
            // Also remove corresponding items from validItems
            Set<Long> excludedTimestamps = transactionsToExclude.stream()
                    .map(com.blackrock.retirement.domain.ParsedTransaction::getTimestamp)
                    .collect(Collectors.toSet());
            validItems.removeIf(item -> {
                try {
                    Date date = dateFormat.parse(item.getDate());
                    return excludedTimestamps.contains(date.getTime());
                } catch (Exception e) {
                    return false;
                }
            });

            List<com.blackrock.retirement.domain.TemporalPeriod> pPeriods = new ArrayList<>();
            if (request.getP() != null) {
                for (FilterPeriodP p : request.getP()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                        long startDate = sdf.parse(p.getStart()).getTime();
                        long endDate = sdf.parse(p.getEnd()).getTime();
                        pPeriods.add(com.blackrock.retirement.domain.TemporalPeriod.builder()
                                .startDate(startDate)
                                .endDate(endDate)
                                .amount(p.getExtra())
                                .periodType("p")
                                .build());
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format in p period", e);
                    }
                }
            }

            List<com.blackrock.retirement.domain.TemporalPeriod> kPeriods = new ArrayList<>();
            if (request.getK() != null) {
                int index = 0;
                for (FilterPeriodK k : request.getK()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                        long startDate = sdf.parse(k.getStart()).getTime();
                        long endDate = sdf.parse(k.getEnd()).getTime();
                        kPeriods.add(com.blackrock.retirement.domain.TemporalPeriod.builder()
                                .startDate(startDate)
                                .endDate(endDate)
                                .periodType("k")
                                .kPeriodId("k_" + index)
                                .build());
                        index++;
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format in k period", e);
                    }
                }
            }

            // Step 4: Apply temporal rules ONLY to valid transactions
            List<FilteredTransaction> filteredTransactions = new ArrayList<>();
            if (!validTransactions.isEmpty()) {
                filteredTransactions = filterService.filterTransactions(
                        validTransactions,
                        qPeriods,
                        pPeriods,
                        kPeriods
                );
            }

            // Step 5: Build response with valid transactions and temporal rules applied
            List<FilterValidTransactionResponse> validResponses = new ArrayList<>();
            SimpleDateFormat responseDateFormat = new SimpleDateFormat(DATE_FORMAT);
            
            for (int i = 0; i < validItems.size(); i++) {
                FilterTransactionItem item = validItems.get(i);
                FilteredTransaction filteredTx = (i < filteredTransactions.size()) ? filteredTransactions.get(i) : null;
                
                Double amount = item.getAmount();
                Double ceiling = item.getCeiling();
                Double remanent = item.getRemanent();
                
                if (ceiling == null && amount != null && amount > 0) {
                    ceiling = Math.ceil(amount / 100.0) * 100.0;
                }
                if (remanent == null && ceiling != null && amount != null) {
                    remanent = ceiling - amount;
                }
                
                boolean inKPeriod = filteredTx != null && 
                                   filteredTx.getKGroupPeriodId() != null && 
                                   !filteredTx.getKGroupPeriodId().isEmpty();
                
                FilterValidTransactionResponse validResponse = FilterValidTransactionResponse.builder()
                        .date(item.getDate())
                        .amount(amount)
                        .ceiling(ceiling)
                        .remanent(remanent)
                        .inKPeriod(inKPeriod)
                        .build();
                validResponses.add(validResponse);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordExecutionTime(executionTime);

            FilterResponse filterResponse = FilterResponse.builder()
                    .valid(validResponses)
                    .invalid(invalidTransactions)
                    .build();

            return ResponseEntity.ok(filterResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error processing filter request: " + e.getMessage(), e);
        }
    }
}
