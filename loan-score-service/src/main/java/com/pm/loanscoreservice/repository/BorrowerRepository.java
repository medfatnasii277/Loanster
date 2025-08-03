package com.pm.loanscoreservice.repository;

import com.pm.loanscoreservice.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Borrower entity operations.
 * Provides basic CRUD operations and custom queries for borrower data.
 */
@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    /**
     * Find borrower by email address.
     * 
     * @param email the borrower's email
     * @return Optional containing the borrower if found
     */
    Optional<Borrower> findByEmail(String email);

    /**
     * Check if borrower exists by email.
     * 
     * @param email the borrower's email
     * @return true if borrower exists
     */
    boolean existsByEmail(String email);
}
