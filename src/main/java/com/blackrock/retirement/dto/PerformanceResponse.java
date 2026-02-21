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
    private Long memoryUsageBytes;      // Current memory usage in bytes
    private Integer threadCount;        // Current number of active threads
    private Long executionTimeMs;       // Last operation execution time
    private String timestamp;           // Response timestamp
}
