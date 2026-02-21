package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for NPS/Index returns endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnsRequest {
    private Double wage;                        // Pre-tax salary/wage
    private Double inflation;                   // Annual inflation rate (e.g., 5.5)
    private Integer age;                        // User's age
    private List<TransactionItem> transactions; // List of transactions
    private List<FilterPeriodQ> q;             // Override periods with fixed amount
    private List<FilterPeriodP> p;             // Bonus/extra amount periods
    private List<FilterPeriodK> k;             // Grouping periods for savings calculation
}
