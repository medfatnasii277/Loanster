package com.pm.loanscoreservice.controller;

import com.pm.loanscoreservice.dto.LoanScoreResponse;
import com.pm.loanscoreservice.dto.LoanScoreStatsResponse;
import com.pm.loanscoreservice.model.LoanScore;
import com.pm.loanscoreservice.repository.LoanScoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for loan score operations.
 * Provides APIs for retrieving loan scores and statistics.
 */
@RestController
@RequestMapping("/api/loan-scores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Score API", description = "APIs for retrieving loan scores and statistics")
public class LoanScoreController {

    private final LoanScoreRepository loanScoreRepository;

    /**
     * Get loan score by application ID.
     * This is the main API that officer-service will call to get loan scores.
     */
    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get loan score by application ID", 
               description = "Retrieve the calculated loan score for a specific loan application")
    public ResponseEntity<LoanScoreResponse> getLoanScoreByApplicationId(
            @Parameter(description = "Loan application ID", required = true)
            @PathVariable Long applicationId) {
        
        log.info("Received request for loan score of application ID: {}", applicationId);
        
        Optional<LoanScore> loanScore = loanScoreRepository.findByApplicationId(applicationId);
        
        if (loanScore.isEmpty()) {
            log.warn("Loan score not found for application ID: {}", applicationId);
            return ResponseEntity.notFound().build();
        }
        
        LoanScoreResponse response = LoanScoreResponse.fromEntity(loanScore.get());
        log.info("Returning loan score for application ID: {} - Score: {} ({})", 
                applicationId, response.getTotalScore(), response.getScoreGrade());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get loan scores by borrower ID.
     */
    @GetMapping("/borrower/{borrowerId}")
    @Operation(summary = "Get loan scores by borrower ID", 
               description = "Retrieve all loan scores for a specific borrower")
    public ResponseEntity<List<LoanScoreResponse>> getLoanScoresByBorrowerId(
            @Parameter(description = "Borrower ID", required = true)
            @PathVariable Long borrowerId) {
        
        log.info("Received request for loan scores of borrower ID: {}", borrowerId);
        
        List<LoanScore> loanScores = loanScoreRepository.findByBorrowerId(borrowerId);
        List<LoanScoreResponse> responses = loanScores.stream()
                .map(LoanScoreResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.info("Returning {} loan scores for borrower ID: {}", responses.size(), borrowerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get loan scores by grade.
     */
    @GetMapping("/grade/{grade}")
    @Operation(summary = "Get loan scores by grade", 
               description = "Retrieve loan scores by grade (EXCELLENT, GOOD, FAIR, POOR)")
    public ResponseEntity<List<LoanScoreResponse>> getLoanScoresByGrade(
            @Parameter(description = "Score grade", required = true)
            @PathVariable String grade,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Received request for loan scores with grade: {}", grade);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("calculatedAt").descending());
        List<LoanScore> loanScores = loanScoreRepository.findByScoreGrade(grade.toUpperCase());
        
        List<LoanScoreResponse> responses = loanScores.stream()
                .map(LoanScoreResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.info("Returning {} loan scores with grade: {}", responses.size(), grade);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get loan scores by risk assessment.
     */
    @GetMapping("/risk/{riskLevel}")
    @Operation(summary = "Get loan scores by risk level", 
               description = "Retrieve loan scores by risk assessment (LOW, MEDIUM, HIGH)")
    public ResponseEntity<List<LoanScoreResponse>> getLoanScoresByRiskLevel(
            @Parameter(description = "Risk level", required = true)
            @PathVariable String riskLevel) {
        
        log.info("Received request for loan scores with risk level: {}", riskLevel);
        
        List<LoanScore> loanScores = loanScoreRepository.findByRiskAssessment(riskLevel.toUpperCase());
        List<LoanScoreResponse> responses = loanScores.stream()
                .map(LoanScoreResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.info("Returning {} loan scores with risk level: {}", responses.size(), riskLevel);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get loan scores within a score range.
     */
    @GetMapping("/range")
    @Operation(summary = "Get loan scores within range", 
               description = "Retrieve loan scores within a specified score range")
    public ResponseEntity<List<LoanScoreResponse>> getLoanScoresByRange(
            @Parameter(description = "Minimum score", required = true)
            @RequestParam Integer minScore,
            @Parameter(description = "Maximum score", required = true)
            @RequestParam Integer maxScore) {
        
        log.info("Received request for loan scores in range: {} - {}", minScore, maxScore);
        
        List<LoanScore> loanScores = loanScoreRepository.findByScoreRange(minScore, maxScore);
        List<LoanScoreResponse> responses = loanScores.stream()
                .map(LoanScoreResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.info("Returning {} loan scores in range: {} - {}", responses.size(), minScore, maxScore);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all loan scores with pagination.
     */
    @GetMapping
    @Operation(summary = "Get all loan scores", 
               description = "Retrieve all loan scores with pagination")
    public ResponseEntity<Page<LoanScoreResponse>> getAllLoanScores(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "calculatedAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Received request for all loan scores - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.toLowerCase().equals("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LoanScore> loanScores = loanScoreRepository.findAll(pageable);
        
        Page<LoanScoreResponse> responses = loanScores.map(LoanScoreResponse::fromEntity);
        
        log.info("Returning {} loan scores (page {} of {})", 
                responses.getNumberOfElements(), responses.getNumber() + 1, responses.getTotalPages());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get loan score statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get loan score statistics", 
               description = "Retrieve aggregated loan score statistics")
    public ResponseEntity<LoanScoreStatsResponse> getLoanScoreStats() {
        
        log.info("Received request for loan score statistics");
        
        List<LoanScore> allScores = loanScoreRepository.findAll();
        
        if (allScores.isEmpty()) {
            log.info("No loan scores found");
            return ResponseEntity.ok(LoanScoreStatsResponse.builder()
                    .totalScores(0L)
                    .averageScore(0.0)
                    .build());
        }
        
        // Calculate statistics
        double averageScore = allScores.stream()
                .mapToInt(LoanScore::getTotalScore)
                .average()
                .orElse(0.0);
        
        int highestScore = allScores.stream()
                .mapToInt(LoanScore::getTotalScore)
                .max()
                .orElse(0);
        
        int lowestScore = allScores.stream()
                .mapToInt(LoanScore::getTotalScore)
                .min()
                .orElse(0);
        
        // Grade distribution
        long excellentCount = allScores.stream().filter(s -> "EXCELLENT".equals(s.getScoreGrade())).count();
        long goodCount = allScores.stream().filter(s -> "GOOD".equals(s.getScoreGrade())).count();
        long fairCount = allScores.stream().filter(s -> "FAIR".equals(s.getScoreGrade())).count();
        long poorCount = allScores.stream().filter(s -> "POOR".equals(s.getScoreGrade())).count();
        
        // Risk distribution
        long lowRiskCount = allScores.stream().filter(s -> "LOW".equals(s.getRiskAssessment())).count();
        long mediumRiskCount = allScores.stream().filter(s -> "MEDIUM".equals(s.getRiskAssessment())).count();
        long highRiskCount = allScores.stream().filter(s -> "HIGH".equals(s.getRiskAssessment())).count();
        
        // Recent scores (last 10)
        List<LoanScoreResponse> recentScores = allScores.stream()
                .sorted((a, b) -> b.getCalculatedAt().compareTo(a.getCalculatedAt()))
                .limit(10)
                .map(LoanScoreResponse::fromEntity)
                .collect(Collectors.toList());
        
        LoanScoreStatsResponse stats = LoanScoreStatsResponse.builder()
                .totalScores((long) allScores.size())
                .averageScore(averageScore)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .excellentCount(excellentCount)
                .goodCount(goodCount)
                .fairCount(fairCount)
                .poorCount(poorCount)
                .lowRiskCount(lowRiskCount)
                .mediumRiskCount(mediumRiskCount)
                .highRiskCount(highRiskCount)
                .recentScores(recentScores)
                .build();
        
        log.info("Returning loan score statistics - Total: {}, Average: {:.2f}", 
                stats.getTotalScores(), stats.getAverageScore());
        
        return ResponseEntity.ok(stats);
    }
}
