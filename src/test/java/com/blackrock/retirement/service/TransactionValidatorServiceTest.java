package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.ParsedTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Unit
 * 2. Validation to be executed: Test duplicate transaction detection and validation logic
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=TransactionValidatorServiceTest
 */
public class TransactionValidatorServiceTest {

    private TransactionValidatorService validatorService;

    @BeforeEach
    public void setUp() {
        validatorService = new TransactionValidatorService();
    }

    @Test
    public void testNoDuplicates() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(2000L).originalAmount(200.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(3000L).originalAmount(300.0).valid(true).build()
        );

        validatorService.validateTransactions(transactions);

        assertTrue(transactions.stream().allMatch(ParsedTransaction::getValid));
    }

    @Test
    public void testDetectDuplicates() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(2000L).originalAmount(200.0).valid(true).build()
        );

        validatorService.validateTransactions(transactions);

        // Second transaction with same timestamp and amount should be invalid
        assertFalse(transactions.get(1).getValid());
        assertEquals("Duplicate transaction: same timestamp and amount", transactions.get(1).getMessage());
    }

    @Test
    public void testSameTimestampDifferentAmounts() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(1000L).originalAmount(150.0).valid(true).build()
        );

        validatorService.validateTransactions(transactions);

        assertTrue(transactions.stream().allMatch(ParsedTransaction::getValid));
    }

    @Test
    public void testEmptyList() {
        List<ParsedTransaction> transactions = new ArrayList<>();

        validatorService.validateTransactions(transactions);

        assertTrue(transactions.isEmpty());
    }

    @Test
    public void testMultipleDuplicates() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build(),
                ParsedTransaction.builder().timestamp(2000L).originalAmount(200.0).valid(true).build()
        );

        validatorService.validateTransactions(transactions);

        assertTrue(transactions.get(0).getValid());
        assertFalse(transactions.get(1).getValid());
        assertFalse(transactions.get(2).getValid());
        assertTrue(transactions.get(3).getValid());
    }

    @Test
    public void testInvalidTransactionSkipped() {
        List<ParsedTransaction> transactions = Arrays.asList(
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(false).message("Already invalid").build(),
                ParsedTransaction.builder().timestamp(1000L).originalAmount(100.0).valid(true).build()
        );

        validatorService.validateTransactions(transactions);

        assertFalse(transactions.get(0).getValid());
        assertTrue(transactions.get(1).getValid());
    }
}
