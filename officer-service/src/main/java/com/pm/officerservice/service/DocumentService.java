package com.pm.officerservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pm.borrowerservice.events.DocumentUploadEvent;
import com.pm.officerservice.dto.DocumentResponse;
import com.pm.officerservice.dto.DocumentStatusUpdateRequest;
import com.pm.officerservice.events.DocumentStatusUpdateEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.model.Document;
import com.pm.officerservice.model.DocumentStatus;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.repository.BorrowerRepository;
import com.pm.officerservice.repository.DocumentRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final BorrowerRepository borrowerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final KafkaEventProducerService kafkaEventProducerService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public void processDocumentUploadEvent(DocumentUploadEvent event) {
        try {
            log.info("Processing document upload event for documentId: {}", event.getDocumentId());

            // Check if document already exists
            if (documentRepository.existsById(event.getDocumentId())) {
                log.warn("Document with ID {} already exists, skipping", event.getDocumentId());
                return;
            }

            // Find the borrower
            Borrower borrower = borrowerRepository.findById(event.getBorrowerId())
                    .orElseThrow(() -> new RuntimeException("Borrower not found with ID: " + event.getBorrowerId()));

            // Find the loan application if provided
            LoanApplication loanApplication = null;
            if (event.getLoanApplicationId() > 0) {
                loanApplication = loanApplicationRepository.findById(event.getLoanApplicationId())
                        .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + event.getLoanApplicationId()));
            }

            // Parse the timestamp
            LocalDateTime uploadedAtSource = parseTimestamp(event.getEventTimestamp());

            // Parse status from event, default to PENDING if not provided or invalid
            DocumentStatus status = DocumentStatus.PENDING;
            try {
                if (!event.getStatus().isEmpty()) {
                    status = DocumentStatus.valueOf(event.getStatus());
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status '{}' in event, defaulting to PENDING", event.getStatus());
            }

            // Create and save document
            Document document = Document.builder()
                    .documentId(event.getDocumentId())
                    .borrower(borrower)
                    .loanApplication(loanApplication)
                    .documentType(event.getDocumentType())
                    .fileName(event.getFileName())
                    .filePath(event.getFilePath())
                    .fileSize(event.getFileSize())
                    .contentType(event.getContentType())
                    .uploadedAtSource(uploadedAtSource)
                    .status(status)
                    .build();

            documentRepository.save(document);
            log.info("Successfully saved document with ID: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Failed to process document upload event for documentId: {}", event.getDocumentId(), e);
            throw e;
        }
    }

    @Transactional
    public void updateDocumentStatus(Long documentId, DocumentStatusUpdateRequest request) {
        try {
            log.info("Updating document status for document ID: {} to status: {}", documentId, request.getNewStatus());

            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

            String oldStatus = document.getStatus() != null ? document.getStatus().name() : "PENDING";
            String newStatus = request.getNewStatus().name();

            // Update the document status (overwrite, not concatenate)
            document.setStatus(request.getNewStatus());
            document.setStatusUpdatedBy(request.getUpdatedBy());
            document.setStatusUpdatedAt(LocalDateTime.now());
            documentRepository.save(document);

            // Create and publish status update event
            DocumentStatusUpdateEvent.Builder eventBuilder = DocumentStatusUpdateEvent.newBuilder()
                    .setDocumentId(documentId)
                    .setBorrowerId(document.getBorrower().getBorrowerId())
                    .setOldStatus(oldStatus)
                    .setNewStatus(newStatus)
                    .setUpdatedBy(request.getUpdatedBy())
                    .setUpdatedAt(LocalDateTime.now().format(FORMATTER))
                    .setEventId(kafkaEventProducerService.generateEventId())
                    .setEventTimestamp(LocalDateTime.now().format(FORMATTER));

            // Add loan application ID if associated
            if (document.getLoanApplication() != null) {
                eventBuilder.setLoanApplicationId(document.getLoanApplication().getApplicationId());
            }

            // Add rejection reason if provided and status is REJECTED
            if (request.getNewStatus() == DocumentStatus.REJECTED && request.getRejectionReason() != null) {
                eventBuilder.setRejectionReason(request.getRejectionReason());
            }

            DocumentStatusUpdateEvent event = eventBuilder.build();
            kafkaEventProducerService.publishDocumentStatusUpdateEvent(event);

            log.info("Successfully updated document status for document ID: {} from {} to {}", 
                    documentId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to update document status for document ID: {}", documentId, e);
            throw e;
        }
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<DocumentResponse> getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .map(DocumentResponse::fromEntity);
    }

    public List<DocumentResponse> getDocumentsByStatus(DocumentStatus status) {
        return documentRepository.findByStatus(status)
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocumentResponse> getDocumentsByBorrowerId(Long borrowerId) {
        return documentRepository.findByBorrowerBorrowerId(borrowerId)
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocumentResponse> getDocumentsByLoanApplicationId(Long applicationId) {
        return documentRepository.findByLoanApplicationApplicationId(applicationId)
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean existsById(Long documentId) {
        return documentRepository.existsById(documentId);
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
