package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for performance endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceResponse {
    private String time;       // Timestamp in format "yyyy-MM-dd HH:mm:ss.SSS"
    private Integer threads;   // Current number of active threads
    private String memory;     // Memory usage in MB in format "XXX.XX"
}
