package com.pm.borrowerservice.repository;

import com.pm.borrowerservice.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    Optional<Borrower> findByEmail(String email);
    Optional<Borrower> findBySsn(String ssn);
} 