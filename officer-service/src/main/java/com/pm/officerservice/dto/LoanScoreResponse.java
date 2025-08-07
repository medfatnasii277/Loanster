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
    private Long applicationId;
    private Long borrowerId;
    private Integer totalScore;
    private String scoreGrade; // EXCELLENT, GOOD, FAIR, POOR
    private String riskAssessment; // LOW, MEDIUM, HIGH
    
    // Individual score components
    private Integer employmentScore;
    private Integer incomeScore;
    private Integer loanAmountScore;
    private Integer interestRateScore;
    private Integer employmentYearsScore;
    private Integer loanTermScore;
    
    // Additional metrics
    private BigDecimal debtToIncomeRatio;
    private String scoringReason;
    private LocalDateTime calculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
