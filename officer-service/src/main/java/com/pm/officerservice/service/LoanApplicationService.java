package com.pm.officerservice.service;

import com.pm.borrowerservice.events.LoanApplicationEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.repository.BorrowerRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final BorrowerRepository borrowerRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public void processLoanApplicationEvent(LoanApplicationEvent event) {
        try {
            log.info("Processing loan application event for applicationId: {}", event.getApplicationId());

            // Check if loan application already exists
            if (loanApplicationRepository.existsById(event.getApplicationId())) {
                log.warn("Loan application with ID {} already exists, skipping", event.getApplicationId());
                return;
            }

            // Find the borrower
            Borrower borrower = borrowerRepository.findById(event.getBorrowerId())
                    .orElseThrow(() -> new RuntimeException("Borrower not found with ID: " + event.getBorrowerId()));

            // Parse the timestamp
            LocalDateTime appliedAtSource = parseTimestamp(event.getAppliedAt());

            // Create and save loan application
            LoanApplication loanApplication = LoanApplication.builder()
                    .applicationId(event.getApplicationId())
                    .borrower(borrower)
                    .loanAmount(BigDecimal.valueOf(event.getLoanAmount()))
                    .loanTermMonths(event.getLoanTermMonths())
                    .loanPurpose(event.getLoanPurpose())
                    .interestRate(BigDecimal.valueOf(event.getInterestRate()))
                    .monthlyPayment(BigDecimal.valueOf(event.getMonthlyPayment()))
                    .status(event.getStatus())
                    .appliedAtSource(appliedAtSource)
                    .build();

            loanApplicationRepository.save(loanApplication);
            log.info("Successfully saved loan application with ID: {}", event.getApplicationId());

        } catch (Exception e) {
            log.error("Failed to process loan application event for applicationId: {}", event.getApplicationId(), e);
            throw e;
        }
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}, using current time", timestamp);
            return LocalDateTime.now();
        }
    }
}
