package com.blackrock.retirement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a financial projection (NPS or Index)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialProjection {
    private Double principal;           // P (grouped by k periods)
    private Double rate;                // r (NPS: 7.11% or Index: 14.49%)
    private Double timeHorizon;         // t (60 - age, or 5 if age >= 60)
    private Double age;                 // User's age
    private Double inflationRate;       // For real value calculation
    private Double futureValue;         // A = P(1+r)^t
    private Double realValue;           // A_real = A / (1 + inflation)^t
    private Double taxBenefit;          // NPS tax benefit (0 for Index)
    private String projectionType;      // "NPS" or "INDEX"
}
