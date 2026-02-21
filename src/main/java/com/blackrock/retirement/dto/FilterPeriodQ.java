package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Q period (override period with fixed amount)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterPeriodQ {
    private Double fixed;           // Fixed override amount
    private String start;           // Start date/time as string (e.g., "2023-10-12 20:15:30")
    private String end;             // End date/time as string (e.g., "2023-10-26 20:15:30")
}
