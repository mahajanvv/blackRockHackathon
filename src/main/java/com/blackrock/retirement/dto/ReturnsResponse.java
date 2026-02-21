package com.blackrock.retirement.dto;

import com.blackrock.retirement.domain.FinancialProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for NPS/Index returns endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnsResponse {
    private FinancialProjection projection;
}
