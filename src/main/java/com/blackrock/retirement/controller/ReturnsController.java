package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.ReturnsRequest;
import com.blackrock.retirement.dto.ReturnsResponse;
import com.blackrock.retirement.service.FinancialProjectionService;
import com.blackrock.retirement.service.PerformanceMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for financial returns calculations
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
@RequiredArgsConstructor
public class ReturnsController {

    private final FinancialProjectionService projectionService;
    private final PerformanceMonitorService performanceMonitor;

    /**
     * Calculate NPS projection with tax benefits
     * POST /blackrock/challenge/v1/returns:nps
     */
    @PostMapping("returns:nps")
    public ResponseEntity<ReturnsResponse> nps(@RequestBody ReturnsRequest request) {
        long startTime = System.currentTimeMillis();

        ReturnsResponse response = projectionService.calculateReturnsNPS(request);

        long executionTime = System.currentTimeMillis() - startTime;
        performanceMonitor.recordExecutionTime(executionTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate Index Fund projection (no tax benefit)
     * POST /blackrock/challenge/v1/returns:index
     */
    @PostMapping("returns:index")
    public ResponseEntity<ReturnsResponse> index(@RequestBody ReturnsRequest request) {
        long startTime = System.currentTimeMillis();

        ReturnsResponse response = projectionService.calculateReturnsIndex(request);

        long executionTime = System.currentTimeMillis() - startTime;
        performanceMonitor.recordExecutionTime(executionTime);

        return ResponseEntity.ok(response);
    }
}
