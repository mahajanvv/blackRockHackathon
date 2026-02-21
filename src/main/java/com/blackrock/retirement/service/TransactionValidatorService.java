package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.ParsedTransaction;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Service for validating transactions (duplicate detection)
 */
@Service
public class TransactionValidatorService {

    /**
     * Validate transactions: flag duplicates (same timestamp + amount)
     */
    public void validateTransactions(List<ParsedTransaction> transactions) {
        Set<String> seen = new HashSet<>();
        Map<String, ParsedTransaction> duplicates = new HashMap<>();

        for (ParsedTransaction tx : transactions) {
            if (!tx.getValid()) {
                continue; // Skip already invalid
            }

            String key = tx.getTimestamp() + ":" + tx.getOriginalAmount();
            if (seen.contains(key)) {
                tx.setValid(false);
                tx.setMessage("Duplicate transaction: same timestamp and amount");
            } else {
                seen.add(key);
            }
        }
    }
}
