package com.pm.officerservice.service;

import com.pm.officerservice.client.LoanScoreClient;
import com.pm.officerservice.dto.LoanScoreResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling loan score operations using Feign client with circuit breaker pattern.
 * Provides methods to retrieve and manage loan scores from the loan score service.
 * Implements resilience patterns including circuit breaker, retry, and timeout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanScoreService {

    private final LoanScoreClient loanScoreClient;
    
    private static final String CIRCUIT_BREAKER_NAME = "loan-score-service";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Loan Score Service is currently unavailable. Please try again later.";

    /**
     * Get loan score for a specific loan application with circuit breaker protection.
     * 
     * @param applicationId the loan application ID
     * @return Optional containing loan score if found, or error response if service is down
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getLoanScoreFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
    public CompletableFuture<Optional<LoanScoreResponse>> getLoanScore(Long applicationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Fetching loan score for application ID: {}", applicationId);
                LoanScoreResponse score = loanScoreClient.getLoanScore(applicationId);
                log.info("Successfully retrieved loan score for application ID: {}. Score: {} ({})", 
                        applicationId, score.getTotalScore(), score.getScoreGrade());
                return Optional.of(score);
            } catch (Exception e) {
                log.warn("Failed to retrieve loan score for application ID: {}. Error: {}", 
                        applicationId, e.getMessage());
                throw e; // Let circuit breaker handle the exception
            }
        });
    }

    /**
     * Fallback method for getLoanScore when circuit breaker is open or service fails.
     */
    public CompletableFuture<Optional<LoanScoreResponse>> getLoanScoreFallback(Long applicationId, Exception ex) {
        log.warn("Circuit breaker activated for loan score service. Application ID: {}, Error: {}", 
                applicationId, ex.getMessage());
        
        // Create an error response that indicates service is down
        LoanScoreResponse errorResponse = createServiceDownResponse(applicationId);
        return CompletableFuture.completedFuture(Optional.of(errorResponse));
    }

    /**
     * Get loan score synchronously for simpler use cases.
     */
    public Optional<LoanScoreResponse> getLoanScoreSync(Long applicationId) {
        try {
            return getLoanScore(applicationId).get();
        } catch (Exception e) {
            log.warn("Failed to get loan score synchronously for application ID: {}", applicationId);
            return Optional.of(createServiceDownResponse(applicationId));
        }
    }

    /**
     * Get all loan scores for a specific borrower with circuit breaker protection.
     * 
     * @param borrowerId the borrower ID
     * @return list of loan scores
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getBorrowerScoresFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public List<LoanScoreResponse> getBorrowerScores(Long borrowerId) {
        try {
            log.info("Fetching loan scores for borrower ID: {}", borrowerId);
            List<LoanScoreResponse> scores = loanScoreClient.getBorrowerScores(borrowerId);
            log.info("Successfully retrieved {} loan scores for borrower ID: {}", scores.size(), borrowerId);
            return scores;
        } catch (Exception e) {
            log.warn("Failed to retrieve loan scores for borrower ID: {}. Error: {}", 
                    borrowerId, e.getMessage());
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Fallback method for getBorrowerScores when circuit breaker is open or service fails.
     */
    public List<LoanScoreResponse> getBorrowerScoresFallback(Long borrowerId, Exception ex) {
        log.warn("Circuit breaker activated for getBorrowerScores. Borrower ID: {}, Error: {}", 
                borrowerId, ex.getMessage());
        return List.of();
    }

    /**
     * Get loan scores by grade with circuit breaker protection.
     * 
     * @param grade the score grade
     * @return list of loan scores with the specified grade
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getScoresByGradeFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public List<LoanScoreResponse> getScoresByGrade(String grade) {
        try {
            log.info("Fetching loan scores by grade: {}", grade);
            List<LoanScoreResponse> scores = loanScoreClient.getScoresByGrade(grade);
            log.info("Successfully retrieved {} loan scores with grade: {}", scores.size(), grade);
            return scores;
        } catch (Exception e) {
            log.warn("Failed to retrieve loan scores by grade: {}. Error: {}", grade, e.getMessage());
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Fallback method for getScoresByGrade when circuit breaker is open or service fails.
     */
    public List<LoanScoreResponse> getScoresByGradeFallback(String grade, Exception ex) {
        log.warn("Circuit breaker activated for getScoresByGrade. Grade: {}, Error: {}", 
                grade, ex.getMessage());
        return List.of();
    }

    /**
     * Get loan scores by risk assessment with circuit breaker protection.
     * 
     * @param risk the risk assessment
     * @return list of loan scores with the specified risk assessment
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getScoresByRiskFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public List<LoanScoreResponse> getScoresByRisk(String risk) {
        try {
            log.info("Fetching loan scores by risk: {}", risk);
            List<LoanScoreResponse> scores = loanScoreClient.getScoresByRisk(risk);
            log.info("Successfully retrieved {} loan scores with risk: {}", scores.size(), risk);
            return scores;
        } catch (Exception e) {
            log.warn("Failed to retrieve loan scores by risk: {}. Error: {}", risk, e.getMessage());
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Fallback method for getScoresByRisk when circuit breaker is open or service fails.
     */
    public List<LoanScoreResponse> getScoresByRiskFallback(String risk, Exception ex) {
        log.warn("Circuit breaker activated for getScoresByRisk. Risk: {}, Error: {}", 
                risk, ex.getMessage());
        return List.of();
    }

    /**
     * Check if loan score service is available with circuit breaker protection.
     * 
     * @return true if service is available
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "isLoanScoreServiceAvailableFallback")
    public boolean isLoanScoreServiceAvailable() {
        try {
            // Try a lightweight health check endpoint
            loanScoreClient.getScoresByGrade("EXCELLENT");
            log.info("Loan score service is available");
            return true;
        } catch (Exception e) {
            log.warn("Loan score service health check failed: {}", e.getMessage());
            throw e; // Let circuit breaker handle
        }
    }

    /**
     * Fallback method for service availability check.
     */
    public boolean isLoanScoreServiceAvailableFallback(Exception ex) {
        log.warn("Circuit breaker activated for service availability check: {}", ex.getMessage());
        return false;
    }

    /**
     * Create a response indicating the service is down.
     */
    private LoanScoreResponse createServiceDownResponse(Long applicationId) {
        LoanScoreResponse response = new LoanScoreResponse();
        response.setLoanApplicationId(applicationId);
        response.setTotalScore(0);
        response.setScoreGrade("SERVICE_DOWN");
        response.setRiskAssessment("UNKNOWN");
        response.setNotes(SERVICE_UNAVAILABLE_MESSAGE);
        response.setCalculatedAt(LocalDateTime.now());
        response.setServiceAvailable(false);
        return response;
    }

    /**
     * Get service status for admin dashboard.
     */
    public ServiceStatus getServiceStatus() {
        boolean available = isLoanScoreServiceAvailable();
        return new ServiceStatus(available, 
                available ? "Service is running normally" : SERVICE_UNAVAILABLE_MESSAGE);
    }

    /**
     * Inner class for service status response.
     */
    public static class ServiceStatus {
        private final boolean available;
        private final String message;

        public ServiceStatus(boolean available, String message) {
            this.available = available;
            this.message = message;
        }

        public boolean isAvailable() { return available; }
        public String getMessage() { return message; }
    }
}
