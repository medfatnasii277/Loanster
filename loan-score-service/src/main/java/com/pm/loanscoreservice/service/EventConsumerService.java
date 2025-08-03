package com.pm.loanscoreservice.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.borrowerservice.events.LoanApplicationEvent;
import com.pm.loanscoreservice.model.Borrower;
import com.pm.loanscoreservice.model.LoanApplication;
import com.pm.loanscoreservice.model.LoanScore;
import com.pm.loanscoreservice.repository.BorrowerRepository;
import com.pm.loanscoreservice.repository.LoanApplicationRepository;
import com.pm.loanscoreservice.repository.LoanScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service responsible for consuming Kafka events and triggering loan score calculations.
 * Listens to borrower and loan application events from other microservices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumerService {

    private final BorrowerRepository borrowerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanScoreRepository loanScoreRepository;
    private final LoanScoringService loanScoringService;

    /**
     * Consume borrower created events.
     * Stores borrower information for future loan score calculations.
     */
    @KafkaListener(topics = "${kafka.topics.borrower-created}")
    @Transactional
    public void consumeBorrowerCreatedEvent(byte[] eventData) {
        try {
            BorrowerCreatedEvent event = BorrowerCreatedEvent.parseFrom(eventData);
            
            log.info("Received borrower created event for borrower ID: {}", event.getBorrowerId());
            
            // Check if borrower already exists
            if (borrowerRepository.existsById(event.getBorrowerId())) {
                log.warn("Borrower with ID {} already exists, skipping", event.getBorrowerId());
                return;
            }

            // Create borrower entity
            Borrower borrower = Borrower.builder()
                    .borrowerId(event.getBorrowerId())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .email(event.getEmail())
                    .phoneNumber(event.getPhoneNumber())
                    .dateOfBirth(event.getDateOfBirth())
                    .ssn(event.getSsn())
                    .address(event.getAddress())
                    .city(event.getCity())
                    .state(event.getState())
                    .zipCode(event.getZipCode())
                    .annualIncome(BigDecimal.valueOf(event.getAnnualIncome()))
                    .employmentStatus(event.getEmploymentStatus())
                    .employerName(event.getEmployerName())
                    .employmentYears(event.getEmploymentYears())
                    .createdAt(parseDateTime(event.getCreatedAt()))
                    .build();

            borrowerRepository.save(borrower);
            log.info("Successfully saved borrower with ID: {}", event.getBorrowerId());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing borrower created event", e);
        } catch (Exception e) {
            log.error("Error processing borrower created event", e);
        }
    }

    /**
     * Consume loan application events.
     * Stores loan application and triggers score calculation.
     */
    @KafkaListener(topics = "${kafka.topics.loan-application}")
    @Transactional
    public void consumeLoanApplicationEvent(byte[] eventData) {
        try {
            LoanApplicationEvent event = LoanApplicationEvent.parseFrom(eventData);
            
            log.info("Received loan application event for application ID: {} and borrower ID: {}", 
                    event.getApplicationId(), event.getBorrowerId());

            // Check if loan application already processed
            if (loanScoreRepository.existsByApplicationId(event.getApplicationId())) {
                log.warn("Loan score already exists for application ID {}, skipping", event.getApplicationId());
                return;
            }

            // Create loan application entity
            LoanApplication loanApplication = LoanApplication.builder()
                    .applicationId(event.getApplicationId())
                    .borrowerId(event.getBorrowerId())
                    .loanAmount(BigDecimal.valueOf(event.getLoanAmount()))
                    .loanTermMonths(event.getLoanTermMonths())
                    .loanPurpose(event.getLoanPurpose())
                    .interestRate(BigDecimal.valueOf(event.getInterestRate()))
                    .monthlyPayment(BigDecimal.valueOf(event.getMonthlyPayment()))
                    .status(event.getStatus())
                    .appliedAt(parseDateTime(event.getAppliedAt()))
                    .build();

            loanApplicationRepository.save(loanApplication);
            log.info("Successfully saved loan application with ID: {}", event.getApplicationId());

            // Trigger score calculation
            calculateAndStoreLoanScore(event.getBorrowerId(), event.getApplicationId());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing loan application event", e);
        } catch (Exception e) {
            log.error("Error processing loan application event", e);
        }
    }

    /**
     * Calculate and store loan score for a loan application.
     */
    private void calculateAndStoreLoanScore(Long borrowerId, Long applicationId) {
        try {
            // Fetch borrower information
            Borrower borrower = borrowerRepository.findById(borrowerId)
                    .orElse(null);
            
            if (borrower == null) {
                log.warn("Borrower with ID {} not found, cannot calculate loan score", borrowerId);
                return;
            }

            // Fetch loan application
            LoanApplication loanApplication = loanApplicationRepository.findById(applicationId)
                    .orElse(null);
            
            if (loanApplication == null) {
                log.warn("Loan application with ID {} not found, cannot calculate loan score", applicationId);
                return;
            }

            // Calculate loan score
            LoanScore loanScore = loanScoringService.calculateLoanScore(borrower, loanApplication);
            
            // Save loan score
            loanScoreRepository.save(loanScore);
            
            log.info("Successfully calculated and saved loan score for application ID: {}. Score: {} ({})", 
                    applicationId, loanScore.getTotalScore(), loanScore.getScoreGrade());

        } catch (Exception e) {
            log.error("Error calculating loan score for application ID: {}", applicationId, e);
        }
    }

    /**
     * Parse datetime string to LocalDateTime.
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
                return LocalDateTime.now();
            }
            
            // Try different date formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(dateTimeString, formatter);
                } catch (Exception ignored) {
                    // Try next formatter
                }
            }
            
            log.warn("Unable to parse datetime: {}, using current time", dateTimeString);
            return LocalDateTime.now();
            
        } catch (Exception e) {
            log.warn("Error parsing datetime: {}, using current time", dateTimeString, e);
            return LocalDateTime.now();
        }
    }
}
