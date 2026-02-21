package com.blackrock.retirement.service;

import com.blackrock.retirement.domain.ParsedTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Unit
 * 2. Validation to be executed: Test transaction parsing with ceiling calculation and remanent computation
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=TransactionParseServiceTest
 */
public class TransactionParseServiceTest {

    private TransactionParseService parseService;

    @BeforeEach
    public void setUp() {
        parseService = new TransactionParseService();
    }

    @Test
    public void testParsePositiveAmount() {
        ParsedTransaction result = parseService.parse(1000L, 150.50);
        
        assertTrue(result.getValid());
        assertEquals(200.0, result.getCeiling());
        assertEquals(49.50, result.getRemanent(), 0.001);
    }

    @Test
    public void testParseZeroAmount() {
        ParsedTransaction result = parseService.parse(2000L, 0.0);
        
        assertTrue(result.getValid());
        assertEquals(0.0, result.getCeiling());
        assertEquals(0.0, result.getRemanent());
    }

    @Test
    public void testParseExactMultipleOf100() {
        ParsedTransaction result = parseService.parse(3000L, 500.0);
        
        assertTrue(result.getValid());
        assertEquals(500.0, result.getCeiling());
        assertEquals(0.0, result.getRemanent());
    }

    @Test
    public void testParseNegativeAmount() {
        ParsedTransaction result = parseService.parse(4000L, -50.0);
        
        assertFalse(result.getValid());
        assertEquals("Negative amounts are not allowed", result.getMessage());
    }

    @Test
    public void testParseNullAmount() {
        ParsedTransaction result = parseService.parse(5000L, null);
        
        assertFalse(result.getValid());
        assertEquals("Negative amounts are not allowed", result.getMessage());
    }

    @Test
    public void testParseSmallPositiveAmount() {
        ParsedTransaction result = parseService.parse(6000L, 1.5);
        
        assertTrue(result.getValid());
        assertEquals(100.0, result.getCeiling());
        assertEquals(98.5, result.getRemanent(), 0.001);
    }

    @Test
    public void testParseLargeAmount() {
        ParsedTransaction result = parseService.parse(7000L, 9999.99);
        
        assertTrue(result.getValid());
        assertEquals(10000.0, result.getCeiling());
        assertEquals(0.01, result.getRemanent(), 0.001);
    }

    @Test
    public void testParseFloatingPointPrecision() {
        // Test for floating-point precision
        ParsedTransaction result = parseService.parse(8000L, 99.99);
        
        assertTrue(result.getValid());
        assertEquals(100.0, result.getCeiling());
        assertTrue(result.getRemanent() > 0 && result.getRemanent() < 1.0);
    }
}
