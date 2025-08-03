package com.pm.loanscoreservice.repository;

import com.pm.loanscoreservice.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LoanApplication entity operations.
 * Provides basic CRUD operations and custom queries for loan application data.
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    /**
     * Find all loan applications for a specific borrower.
     * 
     * @param borrowerId the borrower's ID
     * @return list of loan applications
     */
    List<LoanApplication> findByBorrowerId(Long borrowerId);

    /**
     * Find loan applications by status.
     * 
     * @param status the loan status
     * @return list of loan applications with the specified status
     */
    List<LoanApplication> findByStatus(String status);
}
