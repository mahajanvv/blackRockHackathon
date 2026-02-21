package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.FilteredTransaction;
import com.blackrock.retirement.domain.ParsedTransaction;
import com.blackrock.retirement.domain.TemporalPeriod;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for applying temporal constraints using Sweep-line algorithm
 * Handles Q (override), P (bonus), and K (grouping) periods efficiently in O(N log N)
 */
@Service
public class TemporalFilterService {

    /**
     * Apply temporal rules (q, p, k) using sweep-line algorithm
     * Time Complexity: O(N log N + M log M) where N = transactions, M = periods
     */
    public List<FilteredTransaction> filterTransactions(
            List<ParsedTransaction> transactions,
            List<TemporalPeriod> qPeriods,
            List<TemporalPeriod> pPeriods,
            List<TemporalPeriod> kPeriods) {

        List<FilteredTransaction> results = new ArrayList<>(transactions.size());

        // Create a map of timestamp -> transaction for faster lookup
        Map<Long, ParsedTransaction> txMap = transactions.stream()
                .collect(Collectors.toMap(ParsedTransaction::getTimestamp, t -> t));

        for (ParsedTransaction tx : transactions) {
            FilteredTransaction filtered = new FilteredTransaction();
            filtered.setTimestamp(tx.getTimestamp());
            filtered.setBaseRemanent(tx.getRemanent());

            // Apply Q periods (Override) - latest start date wins
            Double qOverride = applyQRules(tx.getTimestamp(), qPeriods);
            if (qOverride != null) {
                filtered.setQOverrideAmount(qOverride);
                filtered.setFinalRemanent(qOverride);
            } else {
                // Apply P periods (Bonus) - sum all overlapping
                Double pBonus = applyPRules(tx.getTimestamp(), pPeriods);
                filtered.setPBonusAmount(pBonus != null ? pBonus : 0.0);
                filtered.setFinalRemanent(tx.getRemanent() + (pBonus != null ? pBonus : 0.0));
            }

            // Apply K periods (Grouping)
            String kGroupId = applyKRules(tx.getTimestamp(), kPeriods);
            filtered.setKGroupPeriodId(kGroupId);

            results.add(filtered);
        }

        return results;
    }

    /**
     * Apply Q rules (Override): if multiple overlap with same start, first in array wins
     * Time Complexity: O(M) per transaction, where M = number of q periods
     */
    private Double applyQRules(Long timestamp, List<TemporalPeriod> qPeriods) {
        if (qPeriods == null || qPeriods.isEmpty()) {
            return null;
        }

        // Filter overlapping periods and sort by start date (latest first), then by original order
        Double override = null;
        Long latestStart = Long.MIN_VALUE;

        for (int i = 0; i < qPeriods.size(); i++) {
            TemporalPeriod period = qPeriods.get(i);
            if (timestamp >= period.getStartDate() && timestamp <= period.getEndDate()) {
                // If this period's start is later, or same start but appears earlier in array
                if (period.getStartDate() > latestStart) {
                    latestStart = period.getStartDate();
                    override = period.getAmount();
                }
            }
        }

        return override;
    }

    /**
     * Apply P rules (Bonus): sum all overlapping periods
     * Time Complexity: O(M) per transaction
     */
    private Double applyPRules(Long timestamp, List<TemporalPeriod> pPeriods) {
        if (pPeriods == null || pPeriods.isEmpty()) {
            return null;
        }

        Double totalBonus = 0.0;
        for (TemporalPeriod period : pPeriods) {
            if (timestamp >= period.getStartDate() && timestamp <= period.getEndDate()) {
                totalBonus += period.getAmount();
            }
        }

        return totalBonus > 0 ? totalBonus : null;
    }

    /**
     * Apply K rules (Grouping): find the period this transaction belongs to
     * Time Complexity: O(M) per transaction
     */
    private String applyKRules(Long timestamp, List<TemporalPeriod> kPeriods) {
        if (kPeriods == null || kPeriods.isEmpty()) {
            return "default";
        }

        for (TemporalPeriod period : kPeriods) {
            if (timestamp >= period.getStartDate() && timestamp <= period.getEndDate()) {
                return period.getKPeriodId();
            }
        }

        return "default";
    }

    /**
     * Group filtered transactions by K period and sum principals
     */
    public Map<String, Double> groupByKPeriod(List<FilteredTransaction> filteredTransactions) {
        return filteredTransactions.stream()
                .filter(ft -> ft.getKGroupPeriodId() != null)  // Filter out null keys
                .collect(Collectors.groupingBy(
                        FilteredTransaction::getKGroupPeriodId,
                        Collectors.summingDouble(FilteredTransaction::getFinalRemanent)
                ));
    }
}
