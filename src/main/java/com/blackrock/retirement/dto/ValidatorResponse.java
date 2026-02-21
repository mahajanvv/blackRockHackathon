package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response DTO for validator endpoint
 * Contains valid and invalid transaction lists
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatorResponse {
    @JsonProperty("valid")
    private List<ValidatorTransactionResponse> valid;
    
    @JsonProperty("invalid")
    private List<InvalidTransactionResponse> invalid;
}
