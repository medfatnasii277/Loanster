package com.pm.officerservice.service;

import com.pm.officerservice.client.LoanScoreClient;
import com.pm.officerservice.dto.LoanScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling loan score operations using Feign client.
 * Provides methods to retrieve and manage loan scores from the loan score service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanScoreService {

    private final LoanScoreClient loanScoreClient;

    /**
     * Get loan score for a specific loan application.
     * 
     * @param applicationId the loan application ID
     * @return Optional containing loan score if found
     */
    public Optional<LoanScoreResponse> getLoanScore(Long applicationId) {
        try {
            log.info("Fetching loan score for application ID: {}", applicationId);
            LoanScoreResponse score = loanScoreClient.getLoanScore(applicationId);
            log.info("Successfully retrieved loan score for application ID: {}. Score: {} ({})", 
                    applicationId, score.getTotalScore(), score.getScoreGrade());
            return Optional.of(score);
        } catch (Exception e) {
            log.warn("Failed to retrieve loan score for application ID: {}. Error: {}", 
                    applicationId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get all loan scores for a specific borrower.
     * 
     * @param borrowerId the borrower ID
     * @return list of loan scores
     */
    public List<LoanScoreResponse> getBorrowerScores(Long borrowerId) {
        try {
            log.info("Fetching loan scores for borrower ID: {}", borrowerId);
            List<LoanScoreResponse> scores = loanScoreClient.getBorrowerScores(borrowerId);
            log.info("Successfully retrieved {} loan scores for borrower ID: {}", scores.size(), borrowerId);
            return scores;
        } catch (Exception e) {
            log.warn("Failed to retrieve loan scores for borrower ID: {}. Error: {}", 
                    borrowerId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get loan scores by grade.
     * 
     * @param grade the score grade
     * @return list of loan scores with the specified grade
     */
    public List<LoanScoreResponse> getScoresByGrade(String grade) {
        try {
            log.info("Fetching loan scores by grade: {}", grade);
            List<LoanScoreResponse> scores = loanScoreClient.getScoresByGrade(grade);
            log.info("Successfully retrieved {} loan scores with grade: {}", scores.size(), grade);
            return scores;
        } catch (Exception e) {
            log.warn("Failed to retrieve loan scores by grade: {}. Error: {}", grade, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get loan scores by risk assessment.
     * 
     * @param risk the risk assessment
     * @return list of loan scores with the specified risk assessment
     */
    public List<LoanScoreResponse> getScoresByRisk(String risk) {
        try {
            log.info("Fetching loan scores by risk: {}", risk);
            List<LoanScoreResponse> scores = loanScoreClient.getScoresByRisk(risk);
            log.info("Successfully retrieved {} loan scores with risk: {}", scores.size(), risk);
            return scores;
        } catch (Exception e) {
            log.warn("Failed to retrieve loan scores by risk: {}. Error: {}", risk, e.getMessage());
            return List.of();
        }
    }

    /**
     * Check if loan score service is available.
     * 
     * @return true if service is available
     */
    public boolean isLoanScoreServiceAvailable() {
        try {
            // Try to get scores with a dummy grade to test connectivity
            loanScoreClient.getScoresByGrade("EXCELLENT");
            return true;
        } catch (Exception e) {
            log.warn("Loan score service is not available: {}", e.getMessage());
            return false;
        }
    }
}
