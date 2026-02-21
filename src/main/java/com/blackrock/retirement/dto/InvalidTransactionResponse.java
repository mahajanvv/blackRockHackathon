package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for an invalid transaction in validator endpoint
 * Extends ValidatorTransactionResponse with a message field
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvalidTransactionResponse {
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("ceiling")
    private Double ceiling;
    
    @JsonProperty("remanent")
    private Double remanent;
    
    @JsonProperty("message")
    private String message;
}
