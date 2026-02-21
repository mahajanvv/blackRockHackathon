package com.blackrock.retirement.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Represents a temporal constraint period (q, p, or k type)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporalPeriod {
    private Long startDate;      // milliseconds
    private Long endDate;        // milliseconds
    private String periodType;   // "q" (override), "p" (bonus), "k" (grouping)
    private Double amount;       // fixed amount for q, bonus for p, or null for k
    private String kPeriodId;    // identifier for k periods (grouping key)
}
