package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.FinancialProjection;
import com.blackrock.retirement.dto.*;
import com.blackrock.retirement.util.TransactionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Calculate returns with transactions and periods for NPS
     */
    public ReturnsResponse calculateReturnsNPS(ReturnsRequest request) {
        return calculateReturns(request, true);
    }

    /**
     * Calculate returns with transactions and periods for Index
     */
    public ReturnsResponse calculateReturnsIndex(ReturnsRequest request) {
        return calculateReturns(request, false);
    }

    /**
     * Calculate returns with transactions and periods
     * @param request The returns request with transactions and periods
     * @param isNPS true for NPS (with tax benefit), false for Index
     */
    private ReturnsResponse calculateReturns(ReturnsRequest request, boolean isNPS) {
        // Step 1: Validate and filter transactions
        List<TransactionItem> transactions = request.getTransactions();
        List<ValidatedTransaction> validTransactions = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        double totalAmount = 0.0;
        double totalCeiling = 0.0;
        
        if (transactions != null) {
            for (TransactionItem tx : transactions) {
                String key = TransactionUtils.createTransactionKey(tx.getDate(), tx.getAmount());
                
                // Skip invalid amounts or duplicates
                if (!TransactionUtils.isValidAmount(tx.getAmount()) || seenKeys.contains(key)) {
                    continue;
                }
                
                seenKeys.add(key);
                LocalDateTime txDate = TransactionUtils.parseDate(tx.getDate());
                double ceiling = TransactionUtils.calculateCeiling(tx.getAmount());
                double remanent = TransactionUtils.calculateRemanent(tx.getAmount());
                
                validTransactions.add(new ValidatedTransaction(txDate, tx.getAmount(), ceiling, remanent));
                totalAmount += tx.getAmount();
                totalCeiling += ceiling;
            }
        }
        
        // Step 2: Calculate savings by dates for each K period
        List<SavingsByDate> savingsByDates = new ArrayList<>();
        
        if (request.getK() != null && !validTransactions.isEmpty()) {
            double inflationRate = request.getInflation() / 100.0; // Convert percentage to decimal
            double rate = isNPS ? NPS_RATE : INDEX_RATE;
            
            for (FilterPeriodK kPeriod : request.getK()) {
                LocalDateTime kStart = TransactionUtils.parseDate(kPeriod.getStart());
                LocalDateTime kEnd = TransactionUtils.parseDate(kPeriod.getEnd());
                
                // Calculate duration in days and convert to years
                long days = java.time.temporal.ChronoUnit.DAYS.between(kStart, kEnd);
                double years = days / 365.0;
                
                // Calculate amount for this K period
                double periodAmount = 0.0;
                
                for (ValidatedTransaction tx : validTransactions) {
                    // Check if transaction is within K period
                    if (!tx.date.isBefore(kStart) && !tx.date.isAfter(kEnd)) {
                        // Check if transaction is in Q period (if so, skip)
                        boolean inQPeriod = false;
                        if (request.getQ() != null) {
                            for (FilterPeriodQ qPeriod : request.getQ()) {
                                LocalDateTime qStart = TransactionUtils.parseDate(qPeriod.getStart());
                                LocalDateTime qEnd = TransactionUtils.parseDate(qPeriod.getEnd());
                                if (!tx.date.isBefore(qStart) && !tx.date.isAfter(qEnd)) {
                                    inQPeriod = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!inQPeriod) {
                            double txRemanent = tx.remanent;
                            
                            // Check if transaction is in P period (add bonus)
                            if (request.getP() != null) {
                                for (FilterPeriodP pPeriod : request.getP()) {
                                    LocalDateTime pStart = TransactionUtils.parseDate(pPeriod.getStart());
                                    LocalDateTime pEnd = TransactionUtils.parseDate(pPeriod.getEnd());
                                    if (!tx.date.isBefore(pStart) && !tx.date.isAfter(pEnd)) {
                                        txRemanent += pPeriod.getExtra();
                                    }
                                }
                            }
                            
                            periodAmount += txRemanent;
                        }
                    }
                }
                
                // Apply NPS cap if needed: min(amount, 10% of wage, ₹2,00,000)
                double cappedAmount = periodAmount;
                if (isNPS && request.getWage() != null) {
                    double maxByPercentage = request.getWage() * MAX_DEDUCTION_PERCENTAGE;
                    cappedAmount = Math.min(periodAmount, Math.min(maxByPercentage, MAX_NPS_DEDUCTION));
                }
                
                // Calculate profit using compound interest: A = P(1 + r)^t where t = 60 - age
                // Then adjust for inflation: A_real = A / (1 + inflation)^t
                double yearsToRetirement = Math.max(5.0, 60.0 - request.getAge());
                double futureValue = cappedAmount * Math.pow(1 + rate, yearsToRetirement);
                double realValue = futureValue / Math.pow(1 + inflationRate, yearsToRetirement);
                double profit = realValue - cappedAmount;
                
                // Calculate tax benefit for NPS
                double taxBenefit = 0.0;
                if (isNPS && request.getWage() != null && cappedAmount > 0) {
                    taxBenefit = calculateNPSTaxBenefit(cappedAmount, request.getWage());
                }
                
                // Use capped amount for NPS, uncapped for Index
                double displayAmount = isNPS ? cappedAmount : periodAmount;
                
                SavingsByDate savings = SavingsByDate.builder()
                        .start(kPeriod.getStart())
                        .end(kPeriod.getEnd())
                        .amount(Math.round(displayAmount * 100.0) / 100.0)
                        .profit(Math.round(profit * 100.0) / 100.0)
                        .taxBenefit(Math.round(taxBenefit * 100.0) / 100.0)
                        .build();
                
                savingsByDates.add(savings);
            }
        }
        
        return ReturnsResponse.builder()
                .transactionsTotalAmount(Math.round(totalAmount * 100.0) / 100.0)
                .transactionsTotalCeiling(Math.round(totalCeiling * 100.0) / 100.0)
                .savingsByDates(savingsByDates)
                .build();
    }
    
    /**
     * Helper class to hold validated transaction data
     */
    private static class ValidatedTransaction {
        LocalDateTime date;
        double amount;
        double ceiling;
        double remanent;
        
        ValidatedTransaction(LocalDateTime date, double amount, double ceiling, double remanent) {
            this.date = date;
            this.amount = amount;
            this.ceiling = ceiling;
            this.remanent = remanent;
        }
    }
}
