package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtered transaction in response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterTransactionResponse {
    private String date;                // Transaction date/time
    private Double amount;              // Original amount
    private Double ceiling;             // Original ceiling
    private Double baseRemanent;        // Original remanent
    private Double qOverrideAmount;     // Replaced by q rule if applicable
    private Double pBonusAmount;        // Additional amount from p rules
    private Double finalRemanent;       // baseRemanent + pBonusAmount (or qOverride if applied)
    private String kGroupPeriodIndex;   // Index of which k period this belongs to
}
