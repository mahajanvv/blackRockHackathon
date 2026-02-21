package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Request DTO for filter endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterRequest {
    private Double wage;                        // Wage amount
    private List<FilterTransactionItem> transactions;  // Transactions with date, amount, ceiling, remanent
    private List<FilterPeriodQ> q;              // Override periods with fixed amount
    private List<FilterPeriodP> p;              // Bonus periods with extra amount
    private List<FilterPeriodK> k;              // Grouping periods
}
