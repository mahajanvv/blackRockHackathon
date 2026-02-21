package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.PerformanceResponse;
import com.blackrock.retirement.service.PerformanceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for performance monitoring
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
public class PerformanceController {

    @Autowired
    private PerformanceMonitorService performanceMonitor;

    /**
     * Get current performance metrics
     * GET /blackrock/challenge/v1/performance
     */
    @GetMapping("performance")
    public ResponseEntity<PerformanceResponse> getPerformance() {
        PerformanceResponse response = PerformanceResponse.builder()
                .memoryUsageBytes(performanceMonitor.getMemoryUsageBytes())
                .threadCount(performanceMonitor.getThreadCount())
                .executionTimeMs(performanceMonitor.getLastExecutionTimeMs())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        return ResponseEntity.ok(response);
    }
}
