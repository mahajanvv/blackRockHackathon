package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.PerformanceResponse;
import com.blackrock.retirement.service.PerformanceMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for performance monitoring
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceMonitorService performanceMonitor;

    /**
     * Get current performance metrics
     * GET /blackrock/challenge/v1/performance
     */
    @GetMapping("performance")
    public ResponseEntity<PerformanceResponse> getPerformance() {
        // Format timestamp as "yyyy-MM-dd HH:mm:ss.SSS"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Convert memory from bytes to MB with 2 decimal places
        long memoryBytes = performanceMonitor.getMemoryUsageBytes();
        double memoryMB = memoryBytes / (1024.0 * 1024.0);
        String memoryFormatted = String.format("%.2f", memoryMB);
        
        PerformanceResponse response = PerformanceResponse.builder()
                .time(timestamp)
                .threads(performanceMonitor.getThreadCount())
                .memory(memoryFormatted)
                .build();

        return ResponseEntity.ok(response);
    }
}
