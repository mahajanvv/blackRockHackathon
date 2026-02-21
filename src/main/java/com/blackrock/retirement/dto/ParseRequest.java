package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Request DTO for parse endpoint
 * Accepts list of transactions with date (String) and amount (Double)
 * Example: [{"date": "2023-10-12 20:15:30", "amount": 250}, {...}]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParseRequest {
    @JsonProperty("transactions")
    private List<TransactionItem> transactions;
}
