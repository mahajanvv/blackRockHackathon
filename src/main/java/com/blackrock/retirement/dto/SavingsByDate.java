package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for savings by date period in returns response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsByDate {
    private String start;           // Start date/time as string (e.g., "2023-10-12 20:15:30")
    private String end;             // End date/time as string (e.g., "2023-10-26 20:15:30")
    private Double amount;          // Amount saved in this period
    private Double profit;          // Profit earned in this period
    private Double taxBenefit;      // Tax benefit (0 for index)
}
