package com.blackrock.retirement.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for common transaction operations
 */
public final class TransactionUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private TransactionUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Calculate ceiling (round up to nearest 100)
     */
    public static double calculateCeiling(double amount) {
        return Math.ceil(amount / 100.0) * 100.0;
    }

    /**
     * Calculate remanent (ceiling - amount)
     */
    public static double calculateRemanent(double amount) {
        double ceiling = calculateCeiling(amount);
        return ceiling - amount;
    }

    /**
     * Parse date string to LocalDateTime
     */
    public static LocalDateTime parseDate(String dateStr) {
        return LocalDateTime.parse(dateStr, FORMATTER);
    }

    /**
     * Format LocalDateTime to string
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    /**
     * Create transaction key for duplicate detection
     */
    public static String createTransactionKey(String date, Double amount) {
        return date + "|" + amount;
    }

    /**
     * Check if amount is valid (non-negative)
     */
    public static boolean isValidAmount(Double amount) {
        return amount != null && amount >= 0;
    }
}
