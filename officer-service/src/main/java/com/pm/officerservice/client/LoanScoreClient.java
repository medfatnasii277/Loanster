package com.pm.officerservice.client;

import com.pm.officerservice.dto.LoanScoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with the Loan Score Service.
 * Provides methods to retrieve loan scores and risk assessments.
 */
@FeignClient(
    name = "loan-score-service",
    url = "${services.loan-score-service.url}"
)
public interface LoanScoreClient {

    /**
     * Get loan score for a specific loan application.
     * 
     * @param applicationId the loan application ID
     * @return loan score details
     */
    @GetMapping("/api/loan-scores/application/{applicationId}")
    LoanScoreResponse getLoanScore(@PathVariable("applicationId") Long applicationId);

    /**
     * Get all loan scores for a specific borrower.
     * 
     * @param borrowerId the borrower ID
     * @return list of loan scores for the borrower
     */
    @GetMapping("/api/loan-scores/borrower/{borrowerId}")
    List<LoanScoreResponse> getBorrowerScores(@PathVariable("borrowerId") Long borrowerId);

    /**
     * Get loan scores by grade.
     * 
     * @param grade the score grade (EXCELLENT, GOOD, FAIR, POOR)
     * @return list of loan scores with the specified grade
     */
    @GetMapping("/api/loan-scores/grade/{grade}")
    List<LoanScoreResponse> getScoresByGrade(@PathVariable("grade") String grade);

    /**
     * Get loan scores by risk assessment.
     * 
     * @param risk the risk assessment (LOW, MEDIUM, HIGH)
     * @return list of loan scores with the specified risk assessment
     */
    @GetMapping("/api/loan-scores/risk/{risk}")
    List<LoanScoreResponse> getScoresByRisk(@PathVariable("risk") String risk);
}
