package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for each parsed transaction
 * Format: {"date": "2023-10-12 20:15:30", "amount": 250, "ceiling": 300, "remanent": 50}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParseTransactionResponse {
    @JsonProperty("date")
    private String date;  // Format: "yyyy-MM-dd HH:mm:ss"
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("ceiling")
    private Double ceiling;
    
    @JsonProperty("remanent")
    private Double remanent;
}
