package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.FilteredTransaction;
import com.blackrock.retirement.domain.ParsedTransaction;
import com.blackrock.retirement.domain.TemporalPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Performance
 * 2. Validation to be executed: Test temporal filtering with large datasets (scale testing up to 1M records)
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=TemporalFilterPerformanceTest
 */
public class TemporalFilterPerformanceTest {

    private TemporalFilterService filterService;

    @BeforeEach
    public void setUp() {
        filterService = new TemporalFilterService();
    }

    @Test
    public void testPerformanceWith10kTransactions() {
        // Create 10,000 transactions
        List<ParsedTransaction> transactions = generateTransactions(10000);
        List<TemporalPeriod> qPeriods = generateQPeriods(100);
        List<TemporalPeriod> pPeriods = generatePPeriods(100);
        List<TemporalPeriod> kPeriods = generateKPeriods(50);

        long startTime = System.currentTimeMillis();
        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, pPeriods, kPeriods
        );
        long endTime = System.currentTimeMillis();

        assertEquals(10000, result.size());
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 5000, "10k transactions should complete in < 5 seconds");
    }

    @Test
    public void testPerformanceWith100kTransactions() {
        // Create 100,000 transactions
        List<ParsedTransaction> transactions = generateTransactions(100000);
        List<TemporalPeriod> qPeriods = generateQPeriods(1000);
        List<TemporalPeriod> pPeriods = generatePPeriods(1000);
        List<TemporalPeriod> kPeriods = generateKPeriods(500);

        long startTime = System.currentTimeMillis();
        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, pPeriods, kPeriods
        );
        long endTime = System.currentTimeMillis();

        assertEquals(100000, result.size());
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 30000, "100k transactions should complete in < 30 seconds");
    }

    @Test
    public void testMemoryEfficiencyWith500kTransactions() {
        // Test with 500k transactions - ensure no OutOfMemoryError
        List<ParsedTransaction> transactions = generateTransactions(500000);
        List<TemporalPeriod> qPeriods = generateQPeriods(5000);
        List<TemporalPeriod> pPeriods = generatePPeriods(5000);
        List<TemporalPeriod> kPeriods = generateKPeriods(2500);

        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        long startTime = System.currentTimeMillis();
        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, pPeriods, kPeriods
        );
        long endTime = System.currentTimeMillis();

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;

        assertEquals(500000, result.size());
        assertTrue(memoryUsed < 1024 * 1024 * 1024, "Should use < 1GB memory");
    }

    @Test
    public void testGroupingPerformance() {
        // Test grouping performance with large dataset
        List<ParsedTransaction> transactions = generateTransactions(100000);
        List<TemporalPeriod> kPeriods = generateKPeriods(1000);

        List<FilteredTransaction> filteredTransactions = filterService.filterTransactions(
                transactions, null, null, kPeriods
        );

        long startTime = System.currentTimeMillis();
        Map<String, Double> grouped = filterService.groupByKPeriod(filteredTransactions);
        long endTime = System.currentTimeMillis();

        assertTrue(grouped.size() > 0);
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 1000, "Grouping should complete in < 1 second");
    }

    // Helper methods
    private List<ParsedTransaction> generateTransactions(int count) {
        List<ParsedTransaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(ParsedTransaction.builder()
                    .timestamp(System.currentTimeMillis() + i)
                    .originalAmount(50.0 + (i % 150))
                    .ceiling(100.0 + (i % 100) * 100)
                    .remanent(50.0 + (i % 50))
                    .valid(true)
                    .build());
        }
        return transactions;
    }

    private List<TemporalPeriod> generateQPeriods(int count) {
        List<TemporalPeriod> periods = new ArrayList<>();
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            periods.add(TemporalPeriod.builder()
                    .startDate(baseTime + (i * 1000000L))
                    .endDate(baseTime + ((i + 1) * 1000000L) - 1)
                    .amount(100.0 + i)
                    .periodType("q")
                    .build());
        }
        return periods;
    }

    private List<TemporalPeriod> generatePPeriods(int count) {
        List<TemporalPeriod> periods = new ArrayList<>();
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            periods.add(TemporalPeriod.builder()
                    .startDate(baseTime + (i * 800000L))
                    .endDate(baseTime + ((i + 1) * 800000L) - 1)
                    .amount(25.0 + (i % 75))
                    .periodType("p")
                    .build());
        }
        return periods;
    }

    private List<TemporalPeriod> generateKPeriods(int count) {
        List<TemporalPeriod> periods = new ArrayList<>();
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            periods.add(TemporalPeriod.builder()
                    .startDate(baseTime + (i * 2000000L))
                    .endDate(baseTime + ((i + 1) * 2000000L) - 1)
                    .kPeriodId("group_" + i)
                    .periodType("k")
                    .build());
        }
        return periods;
    }
}
