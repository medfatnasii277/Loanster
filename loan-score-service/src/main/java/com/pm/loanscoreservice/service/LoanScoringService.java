package com.pm.loanscoreservice.service;

import com.pm.loanscoreservice.config.ScoringConfig;
import com.pm.loanscoreservice.model.Borrower;
import com.pm.loanscoreservice.model.LoanApplication;
import com.pm.loanscoreservice.model.LoanScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for calculating loan scores based on borrower and loan application data.
 * 
 * SCORING LOGIC DOCUMENTATION:
 * ===========================
 * 
 * The loan score is calculated using a weighted approach with multiple factors:
 * 
 * 1. EMPLOYMENT STATUS (Weight: -50 to +100 points)
 *    - Unemployed: -50 points (high risk)
 *    - Employed: +100 points (stable income)
 *    - Self-employed: +75 points (moderate stability)
 *    - Student: +25 points (potential but low current income)
 *    - Retired: +50 points (fixed income)
 * 
 * 2. ANNUAL INCOME (Weight: Income * 0.001)
 *    - Higher income = higher score
 *    - $50,000 income = +50 points
 *    - $100,000 income = +100 points
 * 
 * 3. LOAN AMOUNT vs INCOME RATIO (Weight: Ratio * -0.5)
 *    - Lower ratio = better score
 *    - If loan amount > annual income, penalty applies
 *    - Ratio of 2.0 = -100 points penalty
 * 
 * 4. INTEREST RATE (Weight: Rate * -10)
 *    - Higher interest rate = risk penalty
 *    - 5% rate = -50 points
 *    - 10% rate = -100 points
 * 
 * 5. EMPLOYMENT YEARS (Weight: Years * +5)
 *    - Longer employment = stability bonus
 *    - 5 years = +25 points
 *    - 10 years = +50 points
 * 
 * 6. LOAN TERM (Weight: Months * -2)
 *    - Longer term = slight penalty (extended risk)
 *    - 12 months = -24 points
 *    - 36 months = -72 points
 * 
 * SCORE GRADES:
 * - EXCELLENT: 750+ (Low risk, prime candidate)
 * - GOOD: 650-749 (Moderate risk, good candidate)
 * - FAIR: 550-649 (Higher risk, requires review)
 * - POOR: <550 (High risk, likely rejection)
 * 
 * RISK ASSESSMENT:
 * - LOW: Score 650+, employed, debt-to-income < 0.3
 * - MEDIUM: Score 450-649 or moderate risk factors
 * - HIGH: Score <450 or high debt-to-income ratio
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanScoringService {

    private final ScoringConfig scoringConfig;

    /**
     * Calculate comprehensive loan score for an application.
     * 
     * @param borrower the borrower information
     * @param loanApplication the loan application details
     * @return calculated loan score with detailed breakdown
     */
    public LoanScore calculateLoanScore(Borrower borrower, LoanApplication loanApplication) {
        log.info("Calculating loan score for application ID: {} and borrower ID: {}", 
                loanApplication.getApplicationId(), borrower.getBorrowerId());

        // Calculate individual score components
        int employmentScore = calculateEmploymentScore(borrower.getEmploymentStatus());
        int incomeScore = calculateIncomeScore(borrower.getAnnualIncome());
        int loanAmountScore = calculateLoanAmountScore(loanApplication.getLoanAmount(), borrower.getAnnualIncome());
        int interestRateScore = calculateInterestRateScore(loanApplication.getInterestRate());
        int employmentYearsScore = calculateEmploymentYearsScore(borrower.getEmploymentYears());
        int loanTermScore = calculateLoanTermScore(loanApplication.getLoanTermMonths());

        // Calculate total score
        int totalScore = employmentScore + incomeScore + loanAmountScore + 
                        interestRateScore + employmentYearsScore + loanTermScore;

        // Determine score grade
        String scoreGrade = determineScoreGrade(totalScore);
        
        // Calculate debt-to-income ratio
        BigDecimal debtToIncomeRatio = calculateDebtToIncomeRatio(
            loanApplication.getMonthlyPayment(), borrower.getAnnualIncome());
        
        // Determine risk assessment
        String riskAssessment = determineRiskAssessment(totalScore, debtToIncomeRatio, borrower.getEmploymentStatus());
        
        // Generate scoring reason
        String scoringReason = generateScoringReason(borrower, loanApplication, 
            employmentScore, incomeScore, loanAmountScore, interestRateScore, 
            employmentYearsScore, loanTermScore, totalScore);

        return LoanScore.builder()
                .applicationId(loanApplication.getApplicationId())
                .borrowerId(borrower.getBorrowerId())
                .totalScore(totalScore)
                .scoreGrade(scoreGrade)
                .employmentScore(employmentScore)
                .incomeScore(incomeScore)
                .loanAmountScore(loanAmountScore)
                .interestRateScore(interestRateScore)
                .employmentYearsScore(employmentYearsScore)
                .loanTermScore(loanTermScore)
                .debtToIncomeRatio(debtToIncomeRatio)
                .riskAssessment(riskAssessment)
                .scoringReason(scoringReason)
                .build();
    }

    /**
     * Calculate employment status score.
     */
    private int calculateEmploymentScore(String employmentStatus) {
        if (employmentStatus == null) return 0;
        
        return switch (employmentStatus.toLowerCase()) {
            case "unemployed" -> scoringConfig.getWeights().getEmployment().getUnemployed();
            case "employed" -> scoringConfig.getWeights().getEmployment().getEmployed();
            case "self-employed", "self_employed" -> scoringConfig.getWeights().getEmployment().getSelfEmployed();
            case "student" -> scoringConfig.getWeights().getEmployment().getStudent();
            case "retired" -> scoringConfig.getWeights().getEmployment().getRetired();
            default -> 0;
        };
    }

    /**
     * Calculate income-based score.
     */
    private int calculateIncomeScore(BigDecimal annualIncome) {
        if (annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        double incomeMultiplier = scoringConfig.getWeights().getIncome().getMultiplier();
        return (int) (annualIncome.doubleValue() * incomeMultiplier);
    }

    /**
     * Calculate loan amount to income ratio score.
     */
    private int calculateLoanAmountScore(BigDecimal loanAmount, BigDecimal annualIncome) {
        if (loanAmount == null || annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return -100; // Penalty for missing data
        }
        
        double ratio = loanAmount.divide(annualIncome, 2, RoundingMode.HALF_UP).doubleValue();
        double ratioWeight = scoringConfig.getWeights().getLoanAmount().getRatio();
        
        return (int) (ratio * 100 * ratioWeight); // Convert to percentage and apply weight
    }

    /**
     * Calculate interest rate penalty score.
     */
    private int calculateInterestRateScore(BigDecimal interestRate) {
        if (interestRate == null) return 0;
        
        int penalty = scoringConfig.getWeights().getInterestRate().getPenalty();
        return (int) (interestRate.doubleValue() * penalty);
    }

    /**
     * Calculate employment years bonus score.
     */
    private int calculateEmploymentYearsScore(Integer employmentYears) {
        if (employmentYears == null || employmentYears < 0) return 0;
        
        int bonus = scoringConfig.getWeights().getEmploymentYears().getBonus();
        return Math.min(employmentYears * bonus, 100); // Cap at 100 points
    }

    /**
     * Calculate loan term penalty score.
     */
    private int calculateLoanTermScore(Integer loanTermMonths) {
        if (loanTermMonths == null || loanTermMonths <= 0) return 0;
        
        int penalty = scoringConfig.getWeights().getLoanTerm().getPenalty();
        return loanTermMonths * penalty;
    }

    /**
     * Determine score grade based on total score.
     */
    private String determineScoreGrade(int totalScore) {
        if (totalScore >= scoringConfig.getThresholds().getExcellent()) {
            return "EXCELLENT";
        } else if (totalScore >= scoringConfig.getThresholds().getGood()) {
            return "GOOD";
        } else if (totalScore >= scoringConfig.getThresholds().getFair()) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    /**
     * Calculate debt-to-income ratio.
     */
    private BigDecimal calculateDebtToIncomeRatio(BigDecimal monthlyPayment, BigDecimal annualIncome) {
        if (monthlyPayment == null || annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal monthlyIncome = annualIncome.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        return monthlyPayment.divide(monthlyIncome, 4, RoundingMode.HALF_UP);
    }

    /**
     * Determine risk assessment based on score and other factors.
     */
    private String determineRiskAssessment(int totalScore, BigDecimal debtToIncomeRatio, String employmentStatus) {
        // High risk conditions
        if (totalScore < 450 || 
            debtToIncomeRatio.compareTo(BigDecimal.valueOf(0.5)) > 0 ||
            "unemployed".equalsIgnoreCase(employmentStatus)) {
            return "HIGH";
        }
        
        // Low risk conditions
        if (totalScore >= 650 && 
            debtToIncomeRatio.compareTo(BigDecimal.valueOf(0.3)) < 0 &&
            "employed".equalsIgnoreCase(employmentStatus)) {
            return "LOW";
        }
        
        // Default to medium risk
        return "MEDIUM";
    }

    /**
     * Generate detailed scoring reason explanation.
     */
    private String generateScoringReason(Borrower borrower, LoanApplication loanApplication,
                                       int employmentScore, int incomeScore, int loanAmountScore,
                                       int interestRateScore, int employmentYearsScore, int loanTermScore,
                                       int totalScore) {
        StringBuilder reason = new StringBuilder();
        reason.append("Score Breakdown:\n");
        reason.append(String.format("• Employment (%s): %+d points\n", 
            borrower.getEmploymentStatus(), employmentScore));
        reason.append(String.format("• Annual Income ($%s): %+d points\n", 
            borrower.getAnnualIncome(), incomeScore));
        reason.append(String.format("• Loan Amount Ratio: %+d points\n", loanAmountScore));
        reason.append(String.format("• Interest Rate (%s%%): %+d points\n", 
            loanApplication.getInterestRate(), interestRateScore));
        reason.append(String.format("• Employment Years (%d): %+d points\n", 
            borrower.getEmploymentYears(), employmentYearsScore));
        reason.append(String.format("• Loan Term (%d months): %+d points\n", 
            loanApplication.getLoanTermMonths(), loanTermScore));
        reason.append(String.format("\nTotal Score: %d", totalScore));
        
        return reason.toString();
    }
}
