package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for K period (grouping period)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterPeriodK {
    private String start;           // Start date/time as string (e.g., "2023-10-12 20:15:30")
    private String end;             // End date/time as string (e.g., "2023-10-26 20:15:30")
}
