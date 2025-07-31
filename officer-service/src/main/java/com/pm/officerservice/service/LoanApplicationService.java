package com.pm.officerservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pm.borrowerservice.events.LoanApplicationEvent;
import com.pm.officerservice.dto.LoanApplicationResponse;
import com.pm.officerservice.dto.LoanStatusUpdateRequest;
import com.pm.officerservice.events.LoanStatusUpdateEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.model.LoanApplicationStatus;
import com.pm.officerservice.repository.BorrowerRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final BorrowerRepository borrowerRepository;
    private final KafkaEventProducerService kafkaEventProducerService;
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

    @Transactional
    public void updateLoanStatus(Long applicationId, LoanStatusUpdateRequest request) {
        try {
            log.info("Updating loan status for application ID: {} to status: {}", applicationId, request.getNewStatus());

            LoanApplication loanApplication = loanApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + applicationId));

            String oldStatus = loanApplication.getStatus();
            String newStatus = request.getNewStatus().name();

            // Update the loan application status (overwrite, not concatenate)
            loanApplication.setStatus(newStatus);
            loanApplication.setStatusUpdatedBy(request.getUpdatedBy());
            loanApplication.setStatusUpdatedAt(LocalDateTime.now());
            loanApplicationRepository.save(loanApplication);

            // Create and publish status update event
            LoanStatusUpdateEvent event = LoanStatusUpdateEvent.newBuilder()
                    .setApplicationId(applicationId)
                    .setBorrowerId(loanApplication.getBorrower().getBorrowerId())
                    .setOldStatus(oldStatus)
                    .setNewStatus(newStatus)
                    .setUpdatedBy(request.getUpdatedBy())
                    .setUpdatedAt(LocalDateTime.now().format(FORMATTER))
                    .setEventId(kafkaEventProducerService.generateEventId())
                    .setEventTimestamp(LocalDateTime.now().format(FORMATTER))
                    .build();

            // Add rejection reason if provided and status is REJECTED
            if (request.getNewStatus() == LoanApplicationStatus.REJECTED && request.getRejectionReason() != null) {
                event = event.toBuilder().setRejectionReason(request.getRejectionReason()).build();
            }

            kafkaEventProducerService.publishLoanStatusUpdateEvent(event);

            log.info("Successfully updated loan status for application ID: {} from {} to {}", 
                    applicationId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to update loan status for application ID: {}", applicationId, e);
            throw e;
        }
    }

    public List<LoanApplicationResponse> getAllLoanApplications() {
        return loanApplicationRepository.findAll()
                .stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<LoanApplicationResponse> getLoanApplicationById(Long applicationId) {
        return loanApplicationRepository.findById(applicationId)
                .map(LoanApplicationResponse::fromEntity);
    }

    public List<LoanApplicationResponse> getLoanApplicationsByStatus(LoanApplicationStatus status) {
        return loanApplicationRepository.findByStatus(status.name())
                .stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<LoanApplicationResponse> getLoanApplicationsByBorrowerId(Long borrowerId) {
        return loanApplicationRepository.findByBorrowerBorrowerId(borrowerId)
                .stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean existsById(Long applicationId) {
        return loanApplicationRepository.existsById(applicationId);
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
