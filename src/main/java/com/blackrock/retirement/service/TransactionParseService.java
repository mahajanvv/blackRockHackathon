package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.ParsedTransaction;
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
        if (amount == null || amount < 0) {
            return ParsedTransaction.builder()
                    .timestamp(timestamp)
                    .originalAmount(amount)
                    .valid(false)
                    .message("Negative amounts are not allowed")
                    .build();
        }

        Double ceiling = Math.ceil(amount / 100.0) * 100.0;
        Double remanent = ceiling - amount;

        return ParsedTransaction.builder()
                .timestamp(timestamp)
                .originalAmount(amount)
                .ceiling(ceiling)
                .remanent(remanent)
                .valid(true)
                .build();
    }
}
