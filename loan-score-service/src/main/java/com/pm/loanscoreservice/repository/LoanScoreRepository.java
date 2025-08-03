package com.pm.loanscoreservice.repository;

import com.pm.loanscoreservice.model.LoanScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LoanScore entity operations.
 * Provides basic CRUD operations and custom queries for loan score data.
 */
@Repository
public interface LoanScoreRepository extends JpaRepository<LoanScore, Long> {

    /**
     * Find loan score by application ID.
     * 
     * @param applicationId the loan application ID
     * @return Optional containing the loan score if found
     */
    Optional<LoanScore> findByApplicationId(Long applicationId);

    /**
     * Find all loan scores for a specific borrower.
     * 
     * @param borrowerId the borrower's ID
     * @return list of loan scores
     */
    List<LoanScore> findByBorrowerId(Long borrowerId);

    /**
     * Find loan scores by grade.
     * 
     * @param scoreGrade the score grade (EXCELLENT, GOOD, FAIR, POOR)
     * @return list of loan scores with the specified grade
     */
    List<LoanScore> findByScoreGrade(String scoreGrade);

    /**
     * Find loan scores by risk assessment.
     * 
     * @param riskAssessment the risk assessment (LOW, MEDIUM, HIGH)
     * @return list of loan scores with the specified risk assessment
     */
    List<LoanScore> findByRiskAssessment(String riskAssessment);

    /**
     * Find loan scores within a score range.
     * 
     * @param minScore minimum score
     * @param maxScore maximum score
     * @return list of loan scores within the range
     */
    @Query("SELECT ls FROM LoanScore ls WHERE ls.totalScore BETWEEN :minScore AND :maxScore")
    List<LoanScore> findByScoreRange(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore);

    /**
     * Check if loan score exists for application.
     * 
     * @param applicationId the loan application ID
     * @return true if score exists
     */
    boolean existsByApplicationId(Long applicationId);
}
