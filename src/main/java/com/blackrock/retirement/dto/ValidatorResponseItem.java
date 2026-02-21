package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response item for validator endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatorResponseItem {
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("ceiling")
    private Double ceiling;
    
    @JsonProperty("remanent")
    private Double remanent;
    
    @JsonProperty("isDuplicate")
    private Boolean isDuplicate;
    
    @JsonProperty("message")
    private String message;
}
