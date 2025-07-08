package com.pm.officerservice.service;

import com.pm.borrowerservice.events.DocumentUploadEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.model.Document;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.repository.BorrowerRepository;
import com.pm.officerservice.repository.DocumentRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final BorrowerRepository borrowerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
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
                    .build();

            documentRepository.save(document);
            log.info("Successfully saved document with ID: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Failed to process document upload event for documentId: {}", event.getDocumentId(), e);
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
