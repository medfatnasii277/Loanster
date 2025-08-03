package com.pm.loanscoreservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a calculated loan score.
 * Stores the detailed scoring information for each loan application.
 */
@Entity
@Table(name = "loan_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "score_grade", nullable = false)
    private String scoreGrade; // EXCELLENT, GOOD, FAIR, POOR

    // Individual score components for transparency
    @Column(name = "employment_score")
    private Integer employmentScore;

    @Column(name = "income_score")
    private Integer incomeScore;

    @Column(name = "loan_amount_score")
    private Integer loanAmountScore;

    @Column(name = "interest_rate_score")
    private Integer interestRateScore;

    @Column(name = "employment_years_score")
    private Integer employmentYearsScore;

    @Column(name = "loan_term_score")
    private Integer loanTermScore;

    // Risk factors
    @Column(name = "debt_to_income_ratio", precision = 5, scale = 2)
    private BigDecimal debtToIncomeRatio;

    @Column(name = "risk_assessment")
    private String riskAssessment; // LOW, MEDIUM, HIGH

    @Column(name = "scoring_reason", columnDefinition = "TEXT")
    private String scoringReason; // Detailed explanation of the score

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.calculatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
