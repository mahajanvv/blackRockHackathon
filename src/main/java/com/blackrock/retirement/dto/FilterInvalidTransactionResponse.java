package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for invalid filtered transaction in response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterInvalidTransactionResponse {
    private String date;                // Transaction date/time
    private Double amount;              // Original amount
    private String message;             // Error message explaining why it's invalid
}
