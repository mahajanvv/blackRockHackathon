package com.blackrock.retirement.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single daily transaction (expense)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Long timestamp;
    private Double amount;
}
