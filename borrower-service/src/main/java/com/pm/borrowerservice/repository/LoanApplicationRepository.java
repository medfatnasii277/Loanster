package com.pm.borrowerservice.repository;

import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByBorrowerId(Long borrowerId);
    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);
    List<LoanApplication> findByBorrowerIdAndStatus(Long borrowerId, LoanApplicationStatus status);
} 