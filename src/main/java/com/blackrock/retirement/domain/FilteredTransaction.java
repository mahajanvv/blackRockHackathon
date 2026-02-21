package com.blackrock.retirement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filtered transaction after applying temporal rules (q, p, k)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilteredTransaction {
    private Long timestamp;
    private Double baseRemanent;        // Original remanent
    private Double qOverrideAmount;     // Replaced by q rule if applicable
    private Double pBonusAmount;        // Additional amount from p rules
    private Double finalRemanent;       // baseRemanent + pBonusAmount (or qOverride if applied)
    private String kGroupPeriodId;      // Which k period this belongs to
}
