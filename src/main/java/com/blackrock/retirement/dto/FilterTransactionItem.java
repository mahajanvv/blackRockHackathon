package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transaction in filter request with date, amount, ceiling, remanent
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterTransactionItem {
    private String date;            // Date/time as string (e.g., "2023-10-12 20:15:30")
    private Double amount;          // Original amount
    private Double ceiling;         // Ceiling amount
    private Double remanent;        // Remanent (ceiling - amount)
}
