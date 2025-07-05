package com.pm.borrowerservice.repository;

import com.pm.borrowerservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByBorrowerId(Long borrowerId);
    List<Document> findByLoanApplicationId(Long loanApplicationId);
} 