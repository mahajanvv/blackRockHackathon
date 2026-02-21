package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Request DTO for validator endpoint
 * Includes wage information and list of transactions with date, amount, ceiling, remanent
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatorRequest {
    @JsonProperty("wage")
    private Double wage;
    
    @JsonProperty("transactions")
    private List<ValidatorTransactionItem> transactions;
}
