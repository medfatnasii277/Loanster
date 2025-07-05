package com.pm.borrowerservice.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoanCalculatorService {
    /**
     * Calculate monthly payment using the formula for an amortizing loan:
     * M = P * (r(1+r)^n) / ((1+r)^n - 1)
     * @param principal Principal amount
     * @param annualInterestRate Annual interest rate (percent)
     * @param termMonths Loan term in months
     * @return monthly payment
     */
    public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualInterestRate, int termMonths) {
        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply((BigDecimal.ONE.add(monthlyRate)).pow(termMonths));
        BigDecimal denominator = (BigDecimal.ONE.add(monthlyRate)).pow(termMonths).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalPayment(BigDecimal monthlyPayment, int termMonths) {
        return monthlyPayment.multiply(BigDecimal.valueOf(termMonths)).setScale(2, RoundingMode.HALF_UP);
    }
} 