package com.blackrock.retirement.dto;

import com.blackrock.retirement.domain.ParsedTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for parse endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParseResponse {
    private java.util.List<ParsedTransaction> transactions;
    private Integer totalCount;
    private Integer validCount;
}
