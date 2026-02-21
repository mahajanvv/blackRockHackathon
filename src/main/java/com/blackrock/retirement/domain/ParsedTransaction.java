package com.blackrock.retirement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of parsing a transaction with ceiling and remanent calculation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedTransaction {
    private Long timestamp;
    private Double originalAmount;
    private Double ceiling;      // Rounded up to nearest 100
    private Double remanent;     // ceiling - originalAmount
    private Boolean valid;       // Flag for validation
    private String message;      // Error message if invalid
}
