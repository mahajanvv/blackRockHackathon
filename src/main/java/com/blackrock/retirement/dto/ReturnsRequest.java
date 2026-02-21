package com.blackrock.retirement.dto;

import com.blackrock.retirement.domain.FinancialProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for NPS/Index returns endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnsRequest {
    private Double principal;           // P (total investment)
    private Double age;                 // User's age
    private Double inflationRate;       // Annual inflation rate (as decimal, e.g., 0.03 for 3%)
    private Double preTaxSalary;        // For tax benefit calculation (NPS only)
}
