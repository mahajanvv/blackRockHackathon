package com.blackrock.retirement.controller;

import com.blackrock.retirement.domain.FinancialProjection;
import com.blackrock.retirement.dto.ReturnsRequest;
import com.blackrock.retirement.dto.ReturnsResponse;
import com.blackrock.retirement.service.FinancialProjectionService;
import com.blackrock.retirement.service.PerformanceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for financial returns calculations
 */
@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ReturnsController {

    @Autowired
    private FinancialProjectionService projectionService;

    @Autowired
    private PerformanceMonitorService performanceMonitor;

    /**
     * Calculate NPS projection with tax benefits
     * POST /blackrock/challenge/v1/returns:nps
     */
    @PostMapping("returns:nps")
    public ResponseEntity<ReturnsResponse> nps(@RequestBody ReturnsRequest request) {
        long startTime = System.currentTimeMillis();

        FinancialProjection projection = projectionService.calculateNPS(
                request.getPrincipal(),
                request.getAge(),
                request.getInflationRate(),
                request.getPreTaxSalary()
        );

        long executionTime = System.currentTimeMillis() - startTime;
        performanceMonitor.recordExecutionTime(executionTime);

        ReturnsResponse response = ReturnsResponse.builder()
                .projection(projection)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate Index Fund projection (no tax benefit)
     * POST /blackrock/challenge/v1/returns:index
     */
    @PostMapping("returns:index")
    public ResponseEntity<ReturnsResponse> index(@RequestBody ReturnsRequest request) {
        long startTime = System.currentTimeMillis();

        FinancialProjection projection = projectionService.calculateIndex(
                request.getPrincipal(),
                request.getAge(),
                request.getInflationRate()
        );

        long executionTime = System.currentTimeMillis() - startTime;
        performanceMonitor.recordExecutionTime(executionTime);

        ReturnsResponse response = ReturnsResponse.builder()
                .projection(projection)
                .build();

        return ResponseEntity.ok(response);
    }
}
