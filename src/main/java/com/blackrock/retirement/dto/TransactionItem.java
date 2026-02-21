package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transaction item in the parse request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItem {
    @JsonProperty("date")
    private String date;  // Format: "2023-10-12 20:15:30"
    
    @JsonProperty("amount")
    private Double amount;
}
