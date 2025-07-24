package com.pm.officerservice.repository;

import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.model.Document;
import com.pm.officerservice.model.DocumentStatus;
import com.pm.officerservice.model.LoanApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    private Borrower borrower1;
    private Borrower borrower2;
    private LoanApplication loanApplication;
    private Document document1;
    private Document document2;
    private Document document3;

    @BeforeEach
    void setUp() {
        borrower1 = Borrower.builder()
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

        borrower2 = Borrower.builder()
                .borrowerId(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("9876543210")
                .dateOfBirth("1985-05-15")
                .ssn("987-65-4321")
                .address("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .zipCode("90001")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .employerName("XYZ Inc")
                .employmentYears(8)
                .createdAtSource(LocalDateTime.now())
                .build();

        borrower1 = entityManager.persistAndFlush(borrower1);
        borrower2 = entityManager.persistAndFlush(borrower2);

        loanApplication = LoanApplication.builder()
                .applicationId(1L)
                .borrower(borrower1)
                .loanAmount(BigDecimal.valueOf(100000))
                .loanTermMonths(36)
                .loanPurpose("Home Purchase")
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(3000))
                .status("PENDING")
                .appliedAtSource(LocalDateTime.now())
                .build();

        loanApplication = entityManager.persistAndFlush(loanApplication);

        document1 = Document.builder()
                .documentId(1L)
                .borrower(borrower1)
                .loanApplication(loanApplication)
                .documentType("INCOME_STATEMENT")
                .fileName("income.pdf")
                .filePath("/documents/income.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.now())
                .build();

        document2 = Document.builder()
                .documentId(2L)
                .borrower(borrower1)
                .loanApplication(loanApplication)
                .documentType("BANK_STATEMENT")
                .fileName("bank.pdf")
                .filePath("/documents/bank.pdf")
                .fileSize(2048L)
                .contentType("application/pdf")
                .status(DocumentStatus.VERIFIED)
                .uploadedAtSource(LocalDateTime.now())
                .build();

        document3 = Document.builder()
                .documentId(3L)
                .borrower(borrower2)
                .loanApplication(null) // No loan application
                .documentType("ID_CARD")
                .fileName("id.pdf")
                .filePath("/documents/id.pdf")
                .fileSize(512L)
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(document1);
        entityManager.persistAndFlush(document2);
        entityManager.persistAndFlush(document3);
    }

    @Test
    void findByBorrowerBorrowerId_ReturnsMatchingDocuments() {
        // When
        List<Document> documentsForBorrower1 = documentRepository.findByBorrowerBorrowerId(1L);
        List<Document> documentsForBorrower2 = documentRepository.findByBorrowerBorrowerId(2L);

        // Then
        assertThat(documentsForBorrower1).hasSize(2);
        assertThat(documentsForBorrower1).extracting(Document::getDocumentId).containsExactly(1L, 2L);
        
        assertThat(documentsForBorrower2).hasSize(1);
        assertThat(documentsForBorrower2.get(0).getDocumentId()).isEqualTo(3L);
    }

    @Test
    void findByBorrowerBorrowerId_NonExistingBorrower_ReturnsEmpty() {
        // When
        List<Document> documents = documentRepository.findByBorrowerBorrowerId(999L);

        // Then
        assertThat(documents).isEmpty();
    }

    @Test
    void findByLoanApplicationApplicationId_ReturnsMatchingDocuments() {
        // When
        List<Document> documents = documentRepository.findByLoanApplicationApplicationId(1L);

        // Then
        assertThat(documents).hasSize(2);
        assertThat(documents).extracting(Document::getDocumentId).containsExactly(1L, 2L);
    }

    @Test
    void findByLoanApplicationApplicationId_NonExistingApplication_ReturnsEmpty() {
        // When
        List<Document> documents = documentRepository.findByLoanApplicationApplicationId(999L);

        // Then
        assertThat(documents).isEmpty();
    }

    @Test
    void findByStatus_ReturnsMatchingDocuments() {
        // When
        List<Document> pendingDocuments = documentRepository.findByStatus(DocumentStatus.PENDING);
        List<Document> verifiedDocuments = documentRepository.findByStatus(DocumentStatus.VERIFIED);

        // Then
        assertThat(pendingDocuments).hasSize(2);
        assertThat(pendingDocuments).extracting(Document::getDocumentId).containsExactly(1L, 3L);
        
        assertThat(verifiedDocuments).hasSize(1);
        assertThat(verifiedDocuments.get(0).getDocumentId()).isEqualTo(2L);
    }

    @Test
    void findByStatus_NonExistingStatus_ReturnsEmpty() {
        // When
        List<Document> documents = documentRepository.findByStatus(DocumentStatus.REJECTED);

        // Then
        assertThat(documents).isEmpty();
    }

    @Test
    void save_CreatesNewDocument() {
        // Given
        Document newDocument = Document.builder()
                .documentId(4L)
                .borrower(borrower1)
                .loanApplication(loanApplication)
                .documentType("TAX_RETURN")
                .fileName("tax.pdf")
                .filePath("/documents/tax.pdf")
                .fileSize(4096L)
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.now())
                .build();

        // When
        Document saved = documentRepository.save(newDocument);

        // Then
        assertThat(saved.getDocumentId()).isEqualTo(4L);
        assertThat(saved.getDocumentType()).isEqualTo("TAX_RETURN");
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void findById_ExistingId_ReturnsDocument() {
        // When
        var foundDocument = documentRepository.findById(1L);

        // Then
        assertThat(foundDocument).isPresent();
        assertThat(foundDocument.get().getDocumentId()).isEqualTo(1L);
        assertThat(foundDocument.get().getDocumentType()).isEqualTo("INCOME_STATEMENT");
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // When
        var foundDocument = documentRepository.findById(999L);

        // Then
        assertThat(foundDocument).isEmpty();
    }

    @Test
    void existsById_ExistingId_ReturnsTrue() {
        // When
        boolean exists = documentRepository.existsById(1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingId_ReturnsFalse() {
        // When
        boolean exists = documentRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
}
