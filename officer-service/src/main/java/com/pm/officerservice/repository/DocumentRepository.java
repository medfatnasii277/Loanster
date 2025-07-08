package com.pm.officerservice.repository;

import com.pm.officerservice.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByBorrowerBorrowerId(Long borrowerId);
    
    List<Document> findByLoanApplicationApplicationId(Long applicationId);
}
