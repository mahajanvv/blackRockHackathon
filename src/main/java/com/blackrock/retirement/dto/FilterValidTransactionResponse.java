package com.blackrock.retirement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for valid filtered transaction in response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterValidTransactionResponse {
    private String date;                // Transaction date/time
    private Double amount;              // Original amount
    private Double ceiling;             // Original ceiling
    private Double remanent;            // Original remanent
    private Boolean inKPeriod;          // Whether transaction falls in a k period
}
