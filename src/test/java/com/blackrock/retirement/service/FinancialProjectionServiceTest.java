package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.FinancialProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Unit
 * 2. Validation to be executed: Test financial projections (NPS/Index), compound interest, tax calculations, and inflation adjustment
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=FinancialProjectionServiceTest
 */
public class FinancialProjectionServiceTest {

    private FinancialProjectionService projectionService;

    @BeforeEach
    public void setUp() {
        projectionService = new FinancialProjectionService();
    }

    @Test
    public void testNPSProjectionUnder60() {
        FinancialProjection projection = projectionService.calculateNPS(
                100000.0,
                30.0,
                0.03,
                500000.0
        );

        assertNotNull(projection);
        assertEquals(100000.0, projection.getPrincipal());
        assertEquals(0.0711, projection.getRate(), 0.0001);
        assertEquals(30.0, projection.getTimeHorizon());
        assertTrue(projection.getFutureValue() > 100000.0);
        assertEquals("NPS", projection.getProjectionType());
    }

    @Test
    public void testNPSProjectionAbove60() {
        FinancialProjection projection = projectionService.calculateNPS(
                100000.0,
                65.0,
                0.03,
                500000.0
        );

        assertNotNull(projection);
        // Time horizon should be capped at 5 years
        assertEquals(5.0, projection.getTimeHorizon());
    }

    @Test
    public void testIndexProjection() {
        FinancialProjection projection = projectionService.calculateIndex(
                100000.0,
                30.0,
                0.03
        );

        assertNotNull(projection);
        assertEquals(100000.0, projection.getPrincipal());
        assertEquals(0.1449, projection.getRate(), 0.0001);
        assertEquals(0.0, projection.getTaxBenefit());
        assertEquals("INDEX", projection.getProjectionType());
    }

    @Test
    public void testCompoundInterestCalculation() {
        // 100,000 at 7.11% for 30 years should be significant
        FinancialProjection projection = projectionService.calculateNPS(
                100000.0,
                30.0,
                0.0,
                0.0
        );

        assertTrue(projection.getFutureValue() > 750000.0);  // 100k * (1.0711)^30 ≈ 785k
    }

    @Test
    public void testInflationAdjustment() {
        FinancialProjection projection = projectionService.calculateNPS(
                100000.0,
                30.0,
                0.03,
                0.0
        );

        // Real value should be less than future value due to inflation
        assertTrue(projection.getRealValue() < projection.getFutureValue());
    }

    @Test
    public void testTaxBenefitCalculationZeroSalary() {
        FinancialProjection projection = projectionService.calculateNPS(
                50000.0,
                30.0,
                0.03,
                0.0
        );

        assertEquals(0.0, projection.getTaxBenefit());
    }

    @Test
    public void testTaxBenefitCalculationLowSalary() {
        // Salary <= 700,000 should have 0 tax
        FinancialProjection projection = projectionService.calculateNPS(
                50000.0,
                30.0,
                0.03,
                600000.0
        );

        assertEquals(0.0, projection.getTaxBenefit());
    }

    @Test
    public void testTaxBenefitCalculationHighSalary() {
        // High salary should have tax benefit
        FinancialProjection projection = projectionService.calculateNPS(
                200000.0,
                30.0,
                0.03,
                2000000.0
        );

        assertTrue(projection.getTaxBenefit() > 0);
    }

    @Test
    public void testTaxBenefitWithinLimits() {
        // Tax benefit deduction cannot exceed 2,00,000 or 10% of salary
        FinancialProjection projection = projectionService.calculateNPS(
                500000.0,  // Investment > max deduction
                30.0,
                0.03,
                2000000.0
        );

        assertTrue(projection.getTaxBenefit() > 0);
        // The deduction should be min(500000, 10% of 2M, 200000) = 200000
    }

    @Test
    public void testTimeHorizonCalculation() {
        // Test various ages
        FinancialProjection proj1 = projectionService.calculateNPS(100000.0, 25.0, 0.03, 0.0);
        assertEquals(35.0, proj1.getTimeHorizon());

        FinancialProjection proj2 = projectionService.calculateNPS(100000.0, 50.0, 0.03, 0.0);
        assertEquals(10.0, proj2.getTimeHorizon());

        FinancialProjection proj3 = projectionService.calculateNPS(100000.0, 70.0, 0.03, 0.0);
        assertEquals(5.0, proj3.getTimeHorizon());
    }

    @Test
    public void testZeroPrincipal() {
        FinancialProjection projection = projectionService.calculateNPS(
                0.0,
                30.0,
                0.03,
                500000.0
        );

        assertEquals(0.0, projection.getFutureValue());
        assertEquals(0.0, projection.getRealValue());
    }
}
