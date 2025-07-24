package com.pm.officerservice.dto;

import com.pm.officerservice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentResponseTest {

    private Borrower borrower;
    private LoanApplication loanApplication;
    private Document document;

    @BeforeEach
    void setUp() {
        borrower = Borrower.builder()
                .borrowerId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(50000.0)
                .employmentStatus("EMPLOYED")
                .employerName("ABC Corp")
                .employmentYears(5)
                .createdAtSource(LocalDateTime.now())
                .build();

        loanApplication = LoanApplication.builder()
                .applicationId(1L)
                .borrower(borrower)
                .loanAmount(BigDecimal.valueOf(100000))
                .loanTermMonths(36)
                .loanPurpose("Home Purchase")
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(3000))
                .status("PENDING")
                .appliedAtSource(LocalDateTime.now())
                .build();

        document = Document.builder()
                .documentId(1L)
                .borrower(borrower)
                .loanApplication(loanApplication)
                .documentType("INCOME_STATEMENT")
                .fileName("income.pdf")
                .filePath("/documents/income.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .status(DocumentStatus.VERIFIED)
                .statusUpdatedBy("officer123")
                .statusUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .uploadedAtSource(LocalDateTime.of(2024, 12, 1, 9, 0))
                .createdAt(LocalDateTime.of(2024, 12, 1, 9, 30))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 10, 30))
                .build();
    }

    @Test
    void fromEntity_WithLoanApplication_CreatesCorrectResponse() {
        // When
        DocumentResponse response = DocumentResponse.fromEntity(document);

        // Then
        assertThat(response.getDocumentId()).isEqualTo(1L);
        assertThat(response.getBorrowerId()).isEqualTo(1L);
        assertThat(response.getBorrowerName()).isEqualTo("John Doe");
        assertThat(response.getLoanApplicationId()).isEqualTo(1L);
        assertThat(response.getDocumentType()).isEqualTo("INCOME_STATEMENT");
        assertThat(response.getFileName()).isEqualTo("income.pdf");
        assertThat(response.getFilePath()).isEqualTo("/documents/income.pdf");
        assertThat(response.getFileSize()).isEqualTo(1024L);
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
        assertThat(response.getStatusUpdatedBy()).isEqualTo("officer123");
        assertThat(response.getStatusUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(response.getUploadedAtSource()).isEqualTo(LocalDateTime.of(2024, 12, 1, 9, 0));
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 12, 1, 9, 30));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 30));
    }

    @Test
    void fromEntity_WithoutLoanApplication_CreatesCorrectResponse() {
        // Given
        Document documentWithoutLoan = Document.builder()
                .documentId(2L)
                .borrower(borrower)
                .loanApplication(null) // No loan application
                .documentType("ID_CARD")
                .fileName("id.pdf")
                .filePath("/documents/id.pdf")
                .fileSize(512L)
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.of(2024, 12, 1, 9, 0))
                .build();

        // When
        DocumentResponse response = DocumentResponse.fromEntity(documentWithoutLoan);

        // Then
        assertThat(response.getDocumentId()).isEqualTo(2L);
        assertThat(response.getBorrowerId()).isEqualTo(1L);
        assertThat(response.getBorrowerName()).isEqualTo("John Doe");
        assertThat(response.getLoanApplicationId()).isNull();
        assertThat(response.getDocumentType()).isEqualTo("ID_CARD");
        assertThat(response.getFileName()).isEqualTo("id.pdf");
        assertThat(response.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void builderAndAccessors_WorkCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        DocumentResponse response = DocumentResponse.builder()
                .documentId(3L)
                .borrowerId(2L)
                .borrowerName("Jane Smith")
                .loanApplicationId(2L)
                .documentType("BANK_STATEMENT")
                .fileName("bank.pdf")
                .filePath("/documents/bank.pdf")
                .fileSize(2048L)
                .contentType("application/pdf")
                .status(DocumentStatus.REJECTED)
                .statusUpdatedBy("officer456")
                .statusUpdatedAt(now)
                .uploadedAtSource(now.minusDays(1))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        // Then
        assertThat(response.getDocumentId()).isEqualTo(3L);
        assertThat(response.getBorrowerId()).isEqualTo(2L);
        assertThat(response.getBorrowerName()).isEqualTo("Jane Smith");
        assertThat(response.getLoanApplicationId()).isEqualTo(2L);
        assertThat(response.getDocumentType()).isEqualTo("BANK_STATEMENT");
        assertThat(response.getFileName()).isEqualTo("bank.pdf");
        assertThat(response.getFilePath()).isEqualTo("/documents/bank.pdf");
        assertThat(response.getFileSize()).isEqualTo(2048L);
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(response.getStatusUpdatedBy()).isEqualTo("officer456");
        assertThat(response.getStatusUpdatedAt()).isEqualTo(now);
        assertThat(response.getUploadedAtSource()).isEqualTo(now.minusDays(1));
        assertThat(response.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void equalsAndHashCode_WorkCorrectly() {
        // Given
        DocumentResponse response1 = DocumentResponse.fromEntity(document);
        DocumentResponse response2 = DocumentResponse.fromEntity(document);
        
        Document differentDocument = Document.builder()
                .documentId(2L)
                .borrower(borrower)
                .loanApplication(loanApplication)
                .documentType("BANK_STATEMENT")
                .fileName("bank.pdf")
                .filePath("/documents/bank.pdf")
                .fileSize(2048L)
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.now())
                .build();
        DocumentResponse response3 = DocumentResponse.fromEntity(differentDocument);

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    void toString_ContainsRelevantInfo() {
        // Given
        DocumentResponse response = DocumentResponse.fromEntity(document);

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("documentId=1");
        assertThat(toString).contains("borrowerName=John Doe");
        assertThat(toString).contains("fileName=income.pdf");
        assertThat(toString).contains("status=VERIFIED");
    }
}
