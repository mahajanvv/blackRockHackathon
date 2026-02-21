package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for NPS/Index returns endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnsResponse {
    private Double transactionsTotalAmount;     // Sum of valid transaction amounts
    private Double transactionsTotalCeiling;    // Sum of valid transaction ceilings
    private List<SavingsByDate> savingsByDates; // Savings grouped by k periods
}
