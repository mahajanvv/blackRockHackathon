package com.blackrock.retirement.service;

import org.springframework.stereotype.Service;

/**
 * Service for tracking performance metrics
 */
@Service
public class PerformanceMonitorService {

    private long lastExecutionTime = 0;
    private Runtime runtime = Runtime.getRuntime();

    /**
     * Record execution time of an operation
     */
    public void recordExecutionTime(long milliseconds) {
        this.lastExecutionTime = milliseconds;
    }

    /**
     * Get current memory usage in bytes
     */
    public Long getMemoryUsageBytes() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Get current thread count
     */
    public Integer getThreadCount() {
        return Thread.activeCount();
    }

    /**
     * Get last execution time
     */
    public Long getLastExecutionTimeMs() {
        return lastExecutionTime;
    }
}
