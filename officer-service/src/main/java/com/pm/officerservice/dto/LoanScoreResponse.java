package com.pm.officerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for loan score information received from the loan score service.
 * Contains comprehensive scoring details for a loan application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanScoreResponse {

    private Long id;
    private Long loanApplicationId; // Updated field name for consistency
    private Long borrowerId;
    private Integer totalScore;
    private String scoreGrade; // EXCELLENT, GOOD, FAIR, POOR, SERVICE_DOWN
    private String riskAssessment; // LOW, MEDIUM, HIGH, UNKNOWN
    
    // Individual score components
    private Integer employmentScore;
    private Integer incomeScore;
    private Integer loanAmountScore;
    private Integer interestRateScore;
    private Integer employmentYearsScore;
    private Integer loanTermScore;
    
    // Score breakdown for detailed view
    private ScoreBreakdown scoreBreakdown;
    
    // Additional metrics
    private BigDecimal debtToIncomeRatio;
    private String scoringReason;
    private String notes; // For error messages when service is down
    private LocalDateTime calculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Service availability flag
    private boolean serviceAvailable = true;

    /**
     * Inner class for detailed score breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreBreakdown {
        private Integer creditScore;
        private Integer income;
        private Integer employment;
        private Integer debtToIncome;
        private Integer loanToValue;
        private Integer creditHistory;
    }
}
