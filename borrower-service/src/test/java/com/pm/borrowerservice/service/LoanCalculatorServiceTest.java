package com.pm.borrowerservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class LoanCalculatorServiceTest {

    private LoanCalculatorService loanCalculatorService;

    @BeforeEach
    void setUp() {
        loanCalculatorService = new LoanCalculatorService();
    }

    @Test
    void calculateMonthlyPayment_WithInterest() {
        // Given
        BigDecimal principal = new BigDecimal("100000"); // $100,000
        BigDecimal annualInterestRate = new BigDecimal("6.0"); // 6% annual
        int termMonths = 360; // 30 years

        // When
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Then
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        // Expected monthly payment for this loan should be around $599.55
        assertTrue(monthlyPayment.compareTo(new BigDecimal("590")) > 0);
        assertTrue(monthlyPayment.compareTo(new BigDecimal("610")) < 0);
        assertEquals(2, monthlyPayment.scale());
    }

    @Test
    void calculateMonthlyPayment_ZeroInterest() {
        // Given
        BigDecimal principal = new BigDecimal("120000"); // $120,000
        BigDecimal annualInterestRate = BigDecimal.ZERO; // 0% interest
        int termMonths = 60; // 5 years

        // When
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Then
        assertNotNull(monthlyPayment);
        BigDecimal expected = principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        assertEquals(expected, monthlyPayment);
        assertEquals(new BigDecimal("2000.00"), monthlyPayment);
    }

    @Test
    void calculateMonthlyPayment_SmallLoan() {
        // Given
        BigDecimal principal = new BigDecimal("5000"); // $5,000
        BigDecimal annualInterestRate = new BigDecimal("4.5"); // 4.5% annual
        int termMonths = 24; // 2 years

        // When
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Then
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(monthlyPayment.compareTo(new BigDecimal("200")) > 0);
        assertTrue(monthlyPayment.compareTo(new BigDecimal("250")) < 0);
        assertEquals(2, monthlyPayment.scale());
    }

    @Test
    void calculateMonthlyPayment_LargeLoan() {
        // Given
        BigDecimal principal = new BigDecimal("500000"); // $500,000
        BigDecimal annualInterestRate = new BigDecimal("7.25"); // 7.25% annual
        int termMonths = 360; // 30 years

        // When
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Then
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(monthlyPayment.compareTo(new BigDecimal("3000")) > 0);
        assertTrue(monthlyPayment.compareTo(new BigDecimal("4000")) < 0);
        assertEquals(2, monthlyPayment.scale());
    }

    @Test
    void calculateTotalPayment_Success() {
        // Given
        BigDecimal monthlyPayment = new BigDecimal("1500.50");
        int termMonths = 60;

        // When
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Then
        assertNotNull(totalPayment);
        BigDecimal expected = monthlyPayment.multiply(BigDecimal.valueOf(termMonths)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, totalPayment);
        assertEquals(new BigDecimal("90030.00"), totalPayment);
        assertEquals(2, totalPayment.scale());
    }

    @Test
    void calculateTotalPayment_SmallPayment() {
        // Given
        BigDecimal monthlyPayment = new BigDecimal("250.75");
        int termMonths = 12;

        // When
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Then
        assertNotNull(totalPayment);
        assertEquals(new BigDecimal("3009.00"), totalPayment);
        assertEquals(2, totalPayment.scale());
    }

    @Test
    void calculateTotalPayment_ZeroPayment() {
        // Given
        BigDecimal monthlyPayment = BigDecimal.ZERO;
        int termMonths = 36;

        // When
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Then
        assertNotNull(totalPayment);
        assertEquals(BigDecimal.ZERO.setScale(2), totalPayment);
        assertEquals(2, totalPayment.scale());
    }

    @Test
    void calculateTotalPayment_OneMonth() {
        // Given
        BigDecimal monthlyPayment = new BigDecimal("2500.00");
        int termMonths = 1;

        // When
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Then
        assertNotNull(totalPayment);
        assertEquals(monthlyPayment, totalPayment);
        assertEquals(2, totalPayment.scale());
    }
}
