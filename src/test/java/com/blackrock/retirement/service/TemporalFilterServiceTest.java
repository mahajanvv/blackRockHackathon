package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.FilteredTransaction;
import com.blackrock.retirement.domain.ParsedTransaction;
import com.blackrock.retirement.domain.TemporalPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Unit
 * 2. Validation to be executed: Test temporal filtering with Q (override), P (bonus), and K (grouping) rules
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=TemporalFilterServiceTest
 */
public class TemporalFilterServiceTest {

    private TemporalFilterService filterService;

    @BeforeEach
    public void setUp() {
        filterService = new TemporalFilterService();
    }

    @Test
    public void testQRuleOverride() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build()
        );

        List<TemporalPeriod> qPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).amount(100.0).periodType("q").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, null, null
        );

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFinalRemanent());
    }

    @Test
    public void testPRuleBonus() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build()
        );

        List<TemporalPeriod> pPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).amount(25.0).periodType("p").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, null, pPeriods, null
        );

        assertEquals(1, result.size());
        assertEquals(75.0, result.get(0).getFinalRemanent());
    }

    @Test
    public void testMultiplePRuleBonus() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build()
        );

        List<TemporalPeriod> pPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).amount(20.0).periodType("p").build(),
                TemporalPeriod.builder().startDate(1200L).endDate(1800L).amount(30.0).periodType("p").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, null, pPeriods, null
        );

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFinalRemanent()); // 50 + 20 + 30
    }

    @Test
    public void testQRulePrecedenceOverP() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build()
        );

        List<TemporalPeriod> qPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).amount(75.0).periodType("q").build()
        );

        List<TemporalPeriod> pPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).amount(25.0).periodType("p").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, pPeriods, null
        );

        assertEquals(1, result.size());
        // Per spec: Q rule overrides first (75), then P rule adds on top (75+25=100)
        assertEquals(100.0, result.get(0).getFinalRemanent());
    }

    @Test
    public void testKRuleGrouping() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build(),
                ParsedTransaction.builder().timestamp(2500L).remanent(75.0).build()
        );

        List<TemporalPeriod> kPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).kPeriodId("group1").periodType("k").build(),
                TemporalPeriod.builder().startDate(2000L).endDate(3000L).kPeriodId("group2").periodType("k").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, null, null, kPeriods
        );

        assertEquals(2, result.size());
        assertEquals("group1", result.get(0).getKGroupPeriodId());
        assertEquals("group2", result.get(1).getKGroupPeriodId());

        Map<String, Double> grouped = filterService.groupByKPeriod(result);
        assertEquals(50.0, grouped.get("group1"));
        assertEquals(75.0, grouped.get("group2"));
    }

    @Test
    public void testQRuleLatestStartWins() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build()
        );

        // Two Q rules with different start dates
        List<TemporalPeriod> qPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(1000L).endDate(2000L).amount(100.0).periodType("q").build(),
                TemporalPeriod.builder().startDate(1200L).endDate(2000L).amount(150.0).periodType("q").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, null, null
        );

        assertEquals(1, result.size());
        // Latest start date (1200L) should win
        assertEquals(150.0, result.get(0).getFinalRemanent());
    }

    @Test
    public void testNoOverlappingPeriods() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1500L).remanent(50.0).build()
        );

        List<TemporalPeriod> qPeriods = Arrays.asList(
                TemporalPeriod.builder().startDate(2000L).endDate(3000L).amount(100.0).periodType("q").build()
        );

        List<FilteredTransaction> result = filterService.filterTransactions(
                transactions, qPeriods, null, null
        );

        assertEquals(1, result.size());
        // No Q rule applies, should use original remanent
        assertEquals(50.0, result.get(0).getFinalRemanent());
    }

    @Test
    public void testEmptyTransactions() {
        List<FilteredTransaction> result = filterService.filterTransactions(
                new ArrayList<>(), null, null, null
        );

        assertTrue(result.isEmpty());
    }
}
