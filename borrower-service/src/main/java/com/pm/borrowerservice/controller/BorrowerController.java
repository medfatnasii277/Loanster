package com.pm.borrowerservice.controller;

import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.dto.DocumentDto;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.service.BorrowerService;
import com.pm.borrowerservice.service.DocumentService;
import com.pm.borrowerservice.service.LoanApplicationService;
import com.pm.borrowerservice.service.LoanCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
@Slf4j
public class BorrowerController {
    private final BorrowerService borrowerService;
    private final LoanApplicationService loanApplicationService;
    private final LoanCalculatorService loanCalculatorService;
    private final DocumentService documentService;

    // Borrower CRUD
    @PostMapping
    public ResponseEntity<BorrowerDto> createBorrower(
            @Valid @RequestBody CreateBorrowerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowerService.createBorrower(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowerDto> getBorrower(@PathVariable Long id) {
        return ResponseEntity.ok(borrowerService.getBorrower(id));
    }

    @GetMapping
    public ResponseEntity<List<BorrowerDto>> getAllBorrowers() {
        return ResponseEntity.ok(borrowerService.getAllBorrowers());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<BorrowerDto> getBorrowerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(borrowerService.getBorrowerByEmail(email));
    }

    // Removed getBorrowerByUserId endpoint and logic

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrower(@PathVariable Long id) {
        borrowerService.deleteBorrower(id);
        return ResponseEntity.noContent().build();
    }

    // Loan Application
    @PostMapping("/{borrowerId}/loans")
    public ResponseEntity<LoanApplicationDto> applyForLoan(
            @PathVariable Long borrowerId,
            @Valid @RequestBody LoanApplicationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanApplicationService.applyForLoan(borrowerId, dto));
    }

    @GetMapping("/{borrowerId}/loans/{applicationId}")
    public ResponseEntity<LoanApplicationDto> getLoanApplication(
            @PathVariable Long borrowerId,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplication(borrowerId, applicationId));
    }
    
    @GetMapping("/{borrowerId}/loans/{applicationId}/details")
    public ResponseEntity<LoanApplicationDto> getLoanApplicationDetails(
            @PathVariable Long borrowerId,
            @PathVariable Long applicationId) {
        log.info("Getting detailed loan application information for borrower: {} and loan: {}", borrowerId, applicationId);
        // We'll reuse the same service method for now, but in a real app this would fetch additional details
        LoanApplicationDto detailedApplication = loanApplicationService.getLoanApplication(borrowerId, applicationId);
        return ResponseEntity.ok(detailedApplication);
    }

    @GetMapping("/{borrowerId}/loans")
    public ResponseEntity<List<LoanApplicationDto>> getLoanApplicationsForBorrower(@PathVariable Long borrowerId) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationsForBorrower(borrowerId));
    }

    @GetMapping("/{borrowerId}/loans/status/{status}")
    public ResponseEntity<List<LoanApplicationDto>> getLoanApplicationsByStatus(
            @PathVariable Long borrowerId,
            @PathVariable LoanApplicationStatus status) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationsByStatus(borrowerId, status));
    }

    // Update Document Status
    @PatchMapping("/{borrowerId}/documents/{documentId}/status")
    public ResponseEntity<Document> updateDocumentStatus(
            @PathVariable Long borrowerId,
            @PathVariable Long documentId,
            @RequestParam("status") com.pm.borrowerservice.entity.DocumentStatus status,
            @RequestHeader(value = "X-Officer-Id", required = false) String officerId) {
        Document updated = documentService.updateDocumentStatus(borrowerId, documentId, status, officerId);
        return ResponseEntity.ok(updated);
    }

    // Loan Calculator
    @PostMapping("/loans/calculate")
    public ResponseEntity<Map<String, Object>> calculateLoan(@RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        BigDecimal interest = new BigDecimal(request.get("interestRate").toString());
        int term = Integer.parseInt(request.get("termMonths").toString());
        BigDecimal monthly = loanCalculatorService.calculateMonthlyPayment(amount, interest, term);
        BigDecimal total = loanCalculatorService.calculateTotalPayment(monthly, term);
        return ResponseEntity.ok(Map.of(
                "monthlyPayment", monthly,
                "totalPayment", total
        ));
    }

    // Document Upload & Management
    @PostMapping("/{borrowerId}/documents")
    public ResponseEntity<DocumentDto> uploadDocument(
            @PathVariable Long borrowerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "loanApplicationId", required = false) Long loanApplicationId
    ) throws IOException {
        Document doc = documentService.uploadDocument(borrowerId, file, documentType, description, loanApplicationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentDto.fromEntity(doc));
    }

    @GetMapping("/{borrowerId}/documents")
    public ResponseEntity<List<DocumentDto>> getDocumentsForBorrower(@PathVariable Long borrowerId) {
        List<DocumentDto> documents = documentService.getDocumentsForBorrower(borrowerId).stream()
                .map(DocumentDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{borrowerId}/documents/{documentId}")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable Long borrowerId,
            @PathVariable Long documentId) {
        Document document = documentService.getDocument(borrowerId, documentId);
        return ResponseEntity.ok(DocumentDto.fromEntity(document));
    }
    
    @GetMapping("/{borrowerId}/documents/{documentId}/details")
    public ResponseEntity<DocumentDto> getDocumentDetails(
            @PathVariable Long borrowerId,
            @PathVariable Long documentId) {
        log.info("Getting detailed document information for borrower: {} and document: {}", borrowerId, documentId);
        // We'll reuse the same service method for now, but in a real app this would fetch additional details
        Document detailedDocument = documentService.getDocument(borrowerId, documentId);
        return ResponseEntity.ok(DocumentDto.fromEntity(detailedDocument));
    }

    @DeleteMapping("/{borrowerId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long borrowerId,
            @PathVariable Long documentId) {
        documentService.deleteDocument(borrowerId, documentId);
        return ResponseEntity.noContent().build();
    }
}
