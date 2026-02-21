package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.FinancialProjection;
import org.springframework.stereotype.Service;

/**
 * Service for calculating financial projections (NPS and Index Fund)
 */
@Service
public class FinancialProjectionService {

    // Constants
    private static final double NPS_RATE = 0.0711;          // 7.11%
    private static final double INDEX_RATE = 0.1449;         // 14.49%
    private static final double MAX_NPS_DEDUCTION = 200000.0; // ₹2,00,000
    private static final double MAX_DEDUCTION_PERCENTAGE = 0.10; // 10% of wage

    /**
     * Calculate NPS projection with tax benefit
     */
    public FinancialProjection calculateNPS(
            Double principal,
            Double age,
            Double inflationRate,
            Double preTaxSalary) {

        // Calculate time horizon
        Double timeHorizon = calculateTimeHorizon(age);

        // Calculate compound interest
        Double futureValue = calculateCompoundInterest(principal, NPS_RATE, timeHorizon);

        // Calculate real value (inflation-adjusted)
        Double realValue = calculateRealValue(futureValue, inflationRate, timeHorizon);

        // Calculate tax benefit
        Double taxBenefit = calculateNPSTaxBenefit(principal, preTaxSalary);

        return FinancialProjection.builder()
                .principal(principal)
                .rate(NPS_RATE)
                .timeHorizon(timeHorizon)
                .age(age)
                .inflationRate(inflationRate)
                .futureValue(futureValue)
                .realValue(realValue)
                .taxBenefit(taxBenefit)
                .projectionType("NPS")
                .build();
    }

    /**
     * Calculate Index Fund projection (no tax benefit)
     */
    public FinancialProjection calculateIndex(
            Double principal,
            Double age,
            Double inflationRate) {

        // Calculate time horizon
        Double timeHorizon = calculateTimeHorizon(age);

        // Calculate compound interest
        Double futureValue = calculateCompoundInterest(principal, INDEX_RATE, timeHorizon);

        // Calculate real value (inflation-adjusted)
        Double realValue = calculateRealValue(futureValue, inflationRate, timeHorizon);

        return FinancialProjection.builder()
                .principal(principal)
                .rate(INDEX_RATE)
                .timeHorizon(timeHorizon)
                .age(age)
                .inflationRate(inflationRate)
                .futureValue(futureValue)
                .realValue(realValue)
                .taxBenefit(0.0)  // No tax benefit for Index
                .projectionType("INDEX")
                .build();
    }

    /**
     * Calculate time horizon: t = 60 - age, minimum 5 years
     */
    private Double calculateTimeHorizon(Double age) {
        if (age >= 60) {
            return 5.0;
        }
        return Math.max(5.0, 60.0 - age);
    }

    /**
     * Calculate compound interest: A = P(1 + r)^t
     */
    private Double calculateCompoundInterest(Double principal, Double rate, Double timeHorizon) {
        return principal * Math.pow(1 + rate, timeHorizon);
    }

    /**
     * Calculate real value: A_real = A / (1 + inflation)^t
     */
    private Double calculateRealValue(Double futureValue, Double inflationRate, Double timeHorizon) {
        return futureValue / Math.pow(1 + inflationRate, timeHorizon);
    }

    /**
     * Calculate NPS tax benefit using progressive tax slabs (India)
     * Tax Slabs:
     * ₹0 - ₹7,00,000: 0%
     * ₹7,00,001 - ₹10,00,000: 10%
     * ₹10,00,001 - ₹12,00,000: 15%
     * ₹12,00,001 - ₹15,00,000: 20%
     * Above ₹15,00,000: 30%
     */
    private Double calculateNPSTaxBenefit(Double invested, Double preTaxSalary) {
        if (preTaxSalary == null || preTaxSalary <= 0) {
            return 0.0;
        }

        // Calculate eligible deduction
        Double maxDeductionByPercentage = preTaxSalary * MAX_DEDUCTION_PERCENTAGE;
        Double eligibleDeduction = Math.min(invested, 
                Math.min(maxDeductionByPercentage, MAX_NPS_DEDUCTION));

        // Calculate tax without deduction
        Double taxWithoutDeduction = calculateTax(preTaxSalary);

        // Calculate tax with deduction
        Double taxableIncomeWithDeduction = preTaxSalary - eligibleDeduction;
        Double taxWithDeduction = calculateTax(taxableIncomeWithDeduction);

        // Tax benefit is the difference
        return Math.max(0, taxWithoutDeduction - taxWithDeduction);
    }

    /**
     * Calculate tax based on Indian progressive tax slabs
     */
    private Double calculateTax(Double income) {
        if (income <= 700000) {
            return 0.0;
        }

        Double tax = 0.0;

        if (income > 700000 && income <= 1000000) {
            tax = (income - 700000) * 0.10;
        } else if (income > 1000000 && income <= 1200000) {
            tax = (1000000 - 700000) * 0.10 + (income - 1000000) * 0.15;
        } else if (income > 1200000 && income <= 1500000) {
            tax = (1000000 - 700000) * 0.10 + (1200000 - 1000000) * 0.15 + (income - 1200000) * 0.20;
        } else { // income > 1500000
            tax = (1000000 - 700000) * 0.10 + (1200000 - 1000000) * 0.15 + 
                  (1500000 - 1200000) * 0.20 + (income - 1500000) * 0.30;
        }

        return tax;
    }
}
