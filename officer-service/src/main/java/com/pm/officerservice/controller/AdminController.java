package com.pm.officerservice.controller;

import com.pm.officerservice.dto.DocumentResponse;
import com.pm.officerservice.dto.DocumentStatusUpdateRequest;
import com.pm.officerservice.dto.LoanApplicationResponse;
import com.pm.officerservice.dto.LoanScoreResponse;
import com.pm.officerservice.dto.LoanStatusUpdateRequest;
import com.pm.officerservice.model.Document;
import com.pm.officerservice.model.DocumentStatus;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.model.LoanApplicationStatus;
import com.pm.officerservice.repository.DocumentRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;
import com.pm.officerservice.service.DocumentService;
import com.pm.officerservice.service.LoanApplicationService;
import com.pm.officerservice.service.LoanScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Admin endpoints for managing loan applications and documents")
public class AdminController {

    private final LoanApplicationService loanApplicationService;
    private final DocumentService documentService;
    private final LoanScoreService loanScoreService;
    private final LoanApplicationRepository loanApplicationRepository;
    private final DocumentRepository documentRepository;
    private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);

    // Loan Application Management


    @GetMapping("/loans")
    @Operation(summary = "Get all loan applications", 
               description = "Retrieve all loan applications in the system")
    public ResponseEntity<List<LoanApplicationResponse>> getAllLoanApplications() {
        log.info("Admin request to get all loan applications");
        List<LoanApplicationResponse> applications = loanApplicationRepository.findAll()
                .stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/loans/{applicationId}")
    @Operation(summary = "Get loan application by ID", 
               description = "Retrieve a specific loan application by its ID")
    public ResponseEntity<LoanApplicationResponse> getLoanApplication(@PathVariable Long applicationId) {
        log.info("Admin request to get loan application with ID: {}", applicationId);
        Optional<LoanApplication> application = loanApplicationRepository.findById(applicationId);
        return application.map(LoanApplicationResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/loans/status/{status}")
    @Operation(summary = "Get loan applications by status", 
               description = "Retrieve all loan applications with a specific status")
    public ResponseEntity<List<LoanApplicationResponse>> getLoanApplicationsByStatus(@PathVariable String status) {
        log.info("Admin request to get loan applications with status: {}", status);
        try {
            LoanApplicationStatus loanStatus = LoanApplicationStatus.valueOf(status.toUpperCase());
            List<LoanApplicationResponse> applications = loanApplicationRepository.findByStatus(loanStatus.name())
                    .stream()
                    .map(LoanApplicationResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid loan status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/loans/{applicationId}/status")
    @Operation(summary = "Update loan application status", 
               description = "Update the status of a loan application and publish event to Kafka")
    public ResponseEntity<Void> updateLoanStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody LoanStatusUpdateRequest request) {
        

        
        loanApplicationService.updateLoanStatus(applicationId, request);
        
        return ResponseEntity.ok().build();
    }

    // Document Management

    @GetMapping("/documents")
    @Operation(summary = "Get all documents", 
               description = "Retrieve all documents in the system")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        log.info("Admin request to get all documents");
        List<DocumentResponse> documents = documentRepository.findAll()
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Get document by ID", 
               description = "Retrieve a specific document by its ID")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long documentId) {
        log.info("Admin request to get document with ID: {}", documentId);
        Optional<Document> document = documentRepository.findById(documentId);
        return document.map(DocumentResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/documents/status/{status}")
    @Operation(summary = "Get documents by status", 
               description = "Retrieve all documents with a specific status")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByStatus(@PathVariable String status) {
        log.info("Admin request to get documents with status: {}", status);
        try {
            DocumentStatus documentStatus = DocumentStatus.valueOf(status.toUpperCase());
            List<DocumentResponse> documents = documentRepository.findByStatus(documentStatus)
                    .stream()
                    .map(DocumentResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/loans/{applicationId}/documents")
    @Operation(summary = "Get documents for loan application", 
               description = "Retrieve all documents associated with a specific loan application")
    public ResponseEntity<List<DocumentResponse>> getDocumentsForLoanApplication(@PathVariable Long applicationId) {
        log.info("Admin request to get documents for loan application ID: {}", applicationId);
        List<DocumentResponse> documents = documentRepository.findByLoanApplicationApplicationId(applicationId)
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/documents/{documentId}/status")
    @Operation(summary = "Update document status", 
               description = "Update the status of a document and publish event to Kafka")
    public ResponseEntity<Void> updateDocumentStatus(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentStatusUpdateRequest request) {
        

        
        documentService.updateDocumentStatus(documentId, request);
        
        return ResponseEntity.ok().build();
    }

    // Status Information

    @GetMapping("/status/loan-statuses")
    @Operation(summary = "Get available loan statuses", 
               description = "Retrieve all available loan application statuses")
    public ResponseEntity<LoanApplicationStatus[]> getAvailableLoanStatuses() {
        log.info("Admin request to get available loan statuses");
        return ResponseEntity.ok(LoanApplicationStatus.values());
    }

    @GetMapping("/status/document-statuses")
    @Operation(summary = "Get available document statuses", 
               description = "Retrieve all available document statuses")
    public ResponseEntity<DocumentStatus[]> getAvailableDocumentStatuses() {
        log.info("Admin request to get available document statuses");
        return ResponseEntity.ok(DocumentStatus.values());
    }

    // Loan Score Management

    @GetMapping("/loans/{applicationId}/score")
    @Operation(summary = "Get loan score for application", 
               description = "Retrieve the calculated loan score for a specific loan application")
    public ResponseEntity<LoanScoreResponse> getLoanScore(@PathVariable Long applicationId) {
        log.info("Admin request to get loan score for application ID: {}", applicationId);
        
        return loanScoreService.getLoanScoreSync(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/borrowers/{borrowerId}/scores")
    @Operation(summary = "Get all scores for borrower", 
               description = "Retrieve all loan scores for a specific borrower")
    public ResponseEntity<List<LoanScoreResponse>> getBorrowerScores(@PathVariable Long borrowerId) {
        log.info("Admin request to get loan scores for borrower ID: {}", borrowerId);
        
        List<LoanScoreResponse> scores = loanScoreService.getBorrowerScores(borrowerId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/scores/grade/{grade}")
    @Operation(summary = "Get scores by grade", 
               description = "Retrieve loan scores filtered by grade (EXCELLENT, GOOD, FAIR, POOR)")
    public ResponseEntity<List<LoanScoreResponse>> getScoresByGrade(@PathVariable String grade) {
        log.info("Admin request to get loan scores by grade: {}", grade);
        
        List<LoanScoreResponse> scores = loanScoreService.getScoresByGrade(grade);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/scores/risk/{risk}")
    @Operation(summary = "Get scores by risk", 
               description = "Retrieve loan scores filtered by risk assessment (LOW, MEDIUM, HIGH)")
    public ResponseEntity<List<LoanScoreResponse>> getScoresByRisk(@PathVariable String risk) {
        log.info("Admin request to get loan scores by risk: {}", risk);
        
        List<LoanScoreResponse> scores = loanScoreService.getScoresByRisk(risk);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/scores/service-status")
    @Operation(summary = "Check loan score service status", 
               description = "Check if the loan score service is available")
    public ResponseEntity<LoanScoreService.ServiceStatus> getLoanScoreServiceStatus() {
        log.info("Admin request to check loan score service status");
        
        LoanScoreService.ServiceStatus status = loanScoreService.getServiceStatus();
        return ResponseEntity.ok(status);
    }

    // Test endpoint to simulate circuit breaker behavior
    @GetMapping("/scores/test-circuit-breaker/{applicationId}")
    @Operation(summary = "Test circuit breaker functionality", 
               description = "Test endpoint to verify circuit breaker behavior")
    public ResponseEntity<LoanScoreResponse> testCircuitBreaker(@PathVariable Long applicationId) {
        log.info("Testing circuit breaker for application ID: {}", applicationId);
        
        return loanScoreService.getLoanScoreSync(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(503).build()); // Service Unavailable
    }
} 