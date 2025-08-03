package com.pm.loanscoreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for loan score statistics and summaries.
 * Provides aggregated information about loan scores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanScoreStatsResponse {

    private Long totalScores;
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
    
    // Grade distribution
    private Long excellentCount;
    private Long goodCount;
    private Long fairCount;
    private Long poorCount;
    
    // Risk distribution
    private Long lowRiskCount;
    private Long mediumRiskCount;
    private Long highRiskCount;
    
    // Recent scores
    private List<LoanScoreResponse> recentScores;
}
