package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.ParsedTransaction;
import com.blackrock.retirement.util.TransactionUtils;
import org.springframework.stereotype.Service;

/**
 * Service for parsing transactions and calculating ceiling/remanent
 */
@Service
public class TransactionParseService {

    /**
     * Parse a transaction: round amount up to nearest 100 and calculate remanent
     */
    public ParsedTransaction parse(Long timestamp, Double amount) {
        if (!TransactionUtils.isValidAmount(amount)) {
            return ParsedTransaction.builder()
                    .timestamp(timestamp)
                    .originalAmount(amount)
                    .valid(false)
                    .message("Negative amounts are not allowed")
                    .build();
        }

        double ceiling = TransactionUtils.calculateCeiling(amount);
        double remanent = TransactionUtils.calculateRemanent(amount);

        return ParsedTransaction.builder()
                .timestamp(timestamp)
                .originalAmount(amount)
                .ceiling(ceiling)
                .remanent(remanent)
                .valid(true)
                .build();
    }
}
