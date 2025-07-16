package com.pm.borrowerservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

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
    void calculateMonthlyPayment_ShouldReturnCorrectAmount_WhenValidInputs() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(10000); // $10,000
        BigDecimal annualInterestRate = BigDecimal.valueOf(5.5); // 5.5%
        int termMonths = 36; // 3 years

        // Act
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Assert
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        // Expected monthly payment for these values should be around $302.89
        assertEquals(BigDecimal.valueOf(302.89), monthlyPayment);
    }

    @Test
    void calculateMonthlyPayment_ShouldReturnCorrectAmount_WhenZeroInterestRate() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal annualInterestRate = BigDecimal.ZERO; // 0%
        int termMonths = 36;

        // Act
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Assert
        assertNotNull(monthlyPayment);
        // With 0% interest, monthly payment should be principal/months
        BigDecimal expected = principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        assertEquals(expected, monthlyPayment);
    }

    @Test
    void calculateMonthlyPayment_ShouldReturnCorrectAmount_WhenHighInterestRate() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(5000);
        BigDecimal annualInterestRate = BigDecimal.valueOf(15.0); // 15%
        int termMonths = 24;

        // Act
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Assert
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(monthlyPayment.compareTo(principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP)) > 0);
    }

    @Test
    void calculateMonthlyPayment_ShouldReturnCorrectAmount_WhenShortTerm() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(1000);
        BigDecimal annualInterestRate = BigDecimal.valueOf(6.0);
        int termMonths = 12; // 1 year

        // Act
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Assert
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        // Should be around $86.07
        assertEquals(BigDecimal.valueOf(86.07), monthlyPayment);
    }

    @Test
    void calculateMonthlyPayment_ShouldReturnCorrectAmount_WhenLongTerm() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(50000);
        BigDecimal annualInterestRate = BigDecimal.valueOf(4.5);
        int termMonths = 360; // 30 years

        // Act
        BigDecimal monthlyPayment = loanCalculatorService.calculateMonthlyPayment(principal, annualInterestRate, termMonths);

        // Assert
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
        // Should be around $253.69
        assertEquals(BigDecimal.valueOf(253.69), monthlyPayment);
    }

    @Test
    void calculateTotalPayment_ShouldReturnCorrectAmount_WhenValidInputs() {
        // Arrange
        BigDecimal monthlyPayment = BigDecimal.valueOf(302.89);
        int termMonths = 36;

        // Act
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Assert
        assertNotNull(totalPayment);
        BigDecimal expected = monthlyPayment.multiply(BigDecimal.valueOf(termMonths)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, totalPayment);
        assertEquals(BigDecimal.valueOf(10904.04), totalPayment);
    }

    @Test
    void calculateTotalPayment_ShouldReturnCorrectAmount_WhenZeroMonthlyPayment() {
        // Arrange
        BigDecimal monthlyPayment = BigDecimal.ZERO;
        int termMonths = 12;

        // Act
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Assert
        assertNotNull(totalPayment);
        assertEquals(BigDecimal.ZERO.setScale(2), totalPayment);
    }

    @Test
    void calculateTotalPayment_ShouldReturnCorrectAmount_WhenSingleMonth() {
        // Arrange
        BigDecimal monthlyPayment = BigDecimal.valueOf(1000.00);
        int termMonths = 1;

        // Act
        BigDecimal totalPayment = loanCalculatorService.calculateTotalPayment(monthlyPayment, termMonths);

        // Assert
        assertNotNull(totalPayment);
        assertEquals(monthlyPayment.setScale(2, RoundingMode.HALF_UP), totalPayment);
    }

    @Test
    void calculateMonthlyPayment_ShouldHandleEdgeCases() {
        // Test with very small principal
        BigDecimal smallPrincipal = BigDecimal.valueOf(100);
        BigDecimal rate = BigDecimal.valueOf(3.0);
        int term = 12;

        BigDecimal result = loanCalculatorService.calculateMonthlyPayment(smallPrincipal, rate, term);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);

        // Test with very high principal
        BigDecimal largePrincipal = BigDecimal.valueOf(1000000);
        result = loanCalculatorService.calculateMonthlyPayment(largePrincipal, rate, term);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculatePayments_ShouldBeConsistent() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(25000);
        BigDecimal rate = BigDecimal.valueOf(7.2);
        int term = 60;

        // Act
        BigDecimal monthly = loanCalculatorService.calculateMonthlyPayment(principal, rate, term);
        BigDecimal total = loanCalculatorService.calculateTotalPayment(monthly, term);

        // Assert
        assertNotNull(monthly);
        assertNotNull(total);
        assertTrue(monthly.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(total.compareTo(principal) > 0); // Total should be more than principal due to interest
        assertTrue(total.compareTo(monthly.multiply(BigDecimal.valueOf(term + 1))) < 0); // Sanity check
    }
}
