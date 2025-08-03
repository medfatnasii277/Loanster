package com.pm.loanscoreservice.dto;

import com.pm.loanscoreservice.model.LoanScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for loan score information.
 * Contains the calculated score and detailed breakdown for API consumers.
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
    private String scoreGrade;
    
    // Score components
    private Integer employmentScore;
    private Integer incomeScore;
    private Integer loanAmountScore;
    private Integer interestRateScore;
    private Integer employmentYearsScore;
    private Integer loanTermScore;
    
    // Risk assessment
    private BigDecimal debtToIncomeRatio;
    private String riskAssessment;
    private String scoringReason;
    
    // Timestamps
    private LocalDateTime calculatedAt;
    private LocalDateTime createdAt;

    /**
     * Convert LoanScore entity to response DTO.
     */
    public static LoanScoreResponse fromEntity(LoanScore loanScore) {
        return LoanScoreResponse.builder()
                .id(loanScore.getId())
                .applicationId(loanScore.getApplicationId())
                .borrowerId(loanScore.getBorrowerId())
                .totalScore(loanScore.getTotalScore())
                .scoreGrade(loanScore.getScoreGrade())
                .employmentScore(loanScore.getEmploymentScore())
                .incomeScore(loanScore.getIncomeScore())
                .loanAmountScore(loanScore.getLoanAmountScore())
                .interestRateScore(loanScore.getInterestRateScore())
                .employmentYearsScore(loanScore.getEmploymentYearsScore())
                .loanTermScore(loanScore.getLoanTermScore())
                .debtToIncomeRatio(loanScore.getDebtToIncomeRatio())
                .riskAssessment(loanScore.getRiskAssessment())
                .scoringReason(loanScore.getScoringReason())
                .calculatedAt(loanScore.getCalculatedAt())
                .createdAt(loanScore.getCreatedAt())
                .build();
    }
}
