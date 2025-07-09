package com.pm.officerservice.repository;

import com.pm.officerservice.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    
    List<LoanApplication> findByBorrowerBorrowerId(Long borrowerId);
    
    List<LoanApplication> findByStatus(String status);
}
