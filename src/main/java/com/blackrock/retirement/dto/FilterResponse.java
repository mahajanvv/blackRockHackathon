package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response DTO for filter endpoint with valid and invalid transaction arrays
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterResponse {
    private List<FilterValidTransactionResponse> valid;      // Valid filtered transactions
    private List<FilterInvalidTransactionResponse> invalid;  // Invalid transactions with error messages
}
