package com.pm.borrowerservice.service;

import com.pm.borrowerservice.events.DocumentStatusUpdateEvent;
import com.pm.borrowerservice.events.LoanStatusUpdateEvent;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.entity.DocumentStatus;
import com.pm.borrowerservice.repository.DocumentRepository;
import com.pm.borrowerservice.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfficerEventConsumerService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final DocumentRepository documentRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Consumes loan status update events from officer service
     * Only processes events for the current borrower's loan applications
     */
    @KafkaListener(topics = "loan-status", groupId = "borrower-service-group")
    @Transactional
    public void handleLoanStatusUpdate(
            @Payload byte[] eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            // Deserialize the protobuf event
            LoanStatusUpdateEvent event = LoanStatusUpdateEvent.parseFrom(eventData);
            
            log.info("Received loan status update event: applicationId={}, borrowerId={}, newStatus={}, updatedBy={}", 
                    event.getApplicationId(), event.getBorrowerId(), event.getNewStatus(), event.getUpdatedBy());

            // Find the loan application in borrower service
            Optional<LoanApplication> loanAppOpt = loanApplicationRepository.findById(event.getApplicationId());
            
            if (loanAppOpt.isEmpty()) {
                log.warn("Loan application with ID {} not found in borrower service, ignoring event", event.getApplicationId());
                return;
            }

            LoanApplication loanApplication = loanAppOpt.get();

            // Security check: Only process events for this borrower's loan applications
            Long loanBorrowerId = loanApplication.getBorrower().getId();
            if (!loanBorrowerId.equals(event.getBorrowerId())) {
                log.warn("Security violation: Received event for borrower {} but loan belongs to borrower {}, ignoring", 
                        event.getBorrowerId(), loanBorrowerId);
                return;
            }

            // Update the loan application status
            LoanApplicationStatus oldStatus = loanApplication.getStatus();
            LoanApplicationStatus newStatus = LoanApplicationStatus.valueOf(event.getNewStatus());
            
            loanApplication.setStatus(newStatus);
            
            // Set the new tracking fields if they exist
            try {
                loanApplication.setStatusUpdatedBy(event.getUpdatedBy());
                loanApplication.setStatusUpdatedAt(parseTimestamp(event.getUpdatedAt()));
            } catch (Exception e) {
                log.debug("Status tracking fields not available, skipping: {}", e.getMessage());
            }
            
            // Set rejection reason if provided
            if (newStatus == LoanApplicationStatus.REJECTED && !event.getRejectionReason().isEmpty()) {
                loanApplication.setRejectionReason(event.getRejectionReason());
            }

            loanApplicationRepository.save(loanApplication);

            log.info("Successfully updated loan application {} status from {} to {} (updated by: {})", 
                    event.getApplicationId(), oldStatus, newStatus, event.getUpdatedBy());

        } catch (Exception e) {
            log.error("Error processing loan status update event from topic {}: {}", topic, e.getMessage());
        }
    }

    /**
     * Consumes document status update events from officer service
     * Only processes events for the current borrower's documents
     */
    @KafkaListener(topics = "documents-status", groupId = "borrower-service-group")
    @Transactional
    public void handleDocumentStatusUpdate(
            @Payload byte[] eventData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            // Deserialize the protobuf event
            DocumentStatusUpdateEvent event = DocumentStatusUpdateEvent.parseFrom(eventData);
            
            log.info("Received document status update event: documentId={}, borrowerId={}, newStatus={}, updatedBy={}", 
                    event.getDocumentId(), event.getBorrowerId(), event.getNewStatus(), event.getUpdatedBy());

            // Find the document in borrower service
            Optional<Document> documentOpt = documentRepository.findById(event.getDocumentId());
            
            if (documentOpt.isEmpty()) {
                log.warn("Document with ID {} not found in borrower service, ignoring event", event.getDocumentId());
                return;
            }

            Document document = documentOpt.get();

            // Security check: Only process events for this borrower's documents
            Long docBorrowerId = document.getBorrower().getId();
            if (!docBorrowerId.equals(event.getBorrowerId())) {
                log.warn("Security violation: Received event for borrower {} but document belongs to borrower {}, ignoring", 
                        event.getBorrowerId(), docBorrowerId);
                return;
            }

            // Update the document status
            DocumentStatus oldStatus = document.getStatus();
            DocumentStatus newStatus = DocumentStatus.valueOf(event.getNewStatus());
            
            document.setStatus(newStatus);
            
            // Set the new tracking fields if they exist
            try {
                document.setStatusUpdatedBy(event.getUpdatedBy());
                document.setStatusUpdatedAt(parseTimestamp(event.getUpdatedAt()));
            } catch (Exception e) {
                log.debug("Status tracking fields not available, skipping: {}", e.getMessage());
            }
            
            // Set rejection reason if provided
            if (newStatus == DocumentStatus.REJECTED && !event.getRejectionReason().isEmpty()) {
                document.setRejectionReason(event.getRejectionReason());
            }

            documentRepository.save(document);

            log.info("Successfully updated document {} status from {} to {} (updated by: {})", 
                    event.getDocumentId(), oldStatus, newStatus, event.getUpdatedBy());

        } catch (Exception e) {
            log.error("Error processing document status update event from topic {}: {}", topic, e.getMessage());
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
