package com.pm.officerservice.controller;

import com.pm.officerservice.dto.DocumentResponse;
import com.pm.officerservice.dto.LoanApplicationResponse;
import com.pm.officerservice.model.Document;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.repository.DocumentRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/borrower")
@RequiredArgsConstructor
@Tag(name = "Borrower Operations", description = "Endpoints for borrowers to access their own data")
public class BorrowerController {

    private final LoanApplicationRepository loanApplicationRepository;
    private final DocumentRepository documentRepository;
    private static final Logger log = LoggerFactory.getLogger(BorrowerController.class);

    @GetMapping("/loans/{applicationId}")
    @Operation(summary = "Get borrower's loan application details",
               description = "Retrieve detailed information for a specific loan application owned by the borrower")
    public ResponseEntity<LoanApplicationResponse> getBorrowerLoanApplication(
            @PathVariable Long applicationId,
            @RequestHeader(value = "X-Borrower-Id", required = false) Long borrowerId) {

        log.info("Borrower request to get loan application with ID: {}", applicationId);

        Optional<LoanApplication> application = loanApplicationRepository.findById(applicationId);
        if (application.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LoanApplication loanApp = application.get();

        // Check if the borrower owns this loan application
        if (borrowerId != null && !loanApp.getBorrower().getBorrowerId().equals(borrowerId)) {
            log.warn("Borrower {} attempted to access loan application {} which they don't own",
                    borrowerId, applicationId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LoanApplicationResponse response = LoanApplicationResponse.fromEntity(loanApp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Get borrower's document details",
               description = "Retrieve detailed information for a specific document owned by the borrower")
    public ResponseEntity<DocumentResponse> getBorrowerDocument(
            @PathVariable Long documentId,
            @RequestHeader(value = "X-Borrower-Id", required = false) Long borrowerId) {

        log.info("Borrower request to get document with ID: {}", documentId);

        Optional<Document> document = documentRepository.findById(documentId);
        if (document.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document doc = document.get();

        // Check if the borrower owns this document
        if (borrowerId != null && !doc.getBorrower().getBorrowerId().equals(borrowerId)) {
            log.warn("Borrower {} attempted to access document {} which they don't own",
                    borrowerId, documentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DocumentResponse response = DocumentResponse.fromEntity(doc);
        return ResponseEntity.ok(response);
    }
}
