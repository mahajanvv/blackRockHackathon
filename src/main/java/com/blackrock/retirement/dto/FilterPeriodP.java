package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for P period (bonus/extra amount period)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterPeriodP {
    private Double extra;           // Extra/bonus amount to add
    private String start;           // Start date/time as string (e.g., "2023-10-28 20:15:30")
    private String end;             // End date/time as string (e.g., "2023-11-31 20:15:30")
}
