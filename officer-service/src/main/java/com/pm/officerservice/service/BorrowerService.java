package com.pm.officerservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.repository.BorrowerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private static final Logger log = LoggerFactory.getLogger(BorrowerService.class);

    private final BorrowerRepository borrowerRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public void processBorrowerCreatedEvent(BorrowerCreatedEvent event) {
        try {
            log.info("Processing borrower created event for borrowerId: {}", event.getBorrowerId());

            // Check if borrower already exists
            if (borrowerRepository.existsById(event.getBorrowerId())) {
                log.warn("Borrower with ID {} already exists, skipping", event.getBorrowerId());
                return;
            }

            // Parse the timestamp
            LocalDateTime createdAtSource = parseTimestamp(event.getCreatedAt());

            // Create and save borrower
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
                    .annualIncome(event.getAnnualIncome())
                    .employmentStatus(event.getEmploymentStatus())
                    .employerName(event.getEmployerName())
                    .employmentYears(event.getEmploymentYears())
                    .createdAtSource(createdAtSource)
                    .build();

            borrowerRepository.save(borrower);
            log.info("Successfully saved borrower with ID: {}", event.getBorrowerId());

        } catch (Exception e) {
            log.error("Failed to process borrower created event for borrowerId: {}", event.getBorrowerId(), e);
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
