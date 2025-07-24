package com.pm.officerservice.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @InjectMocks
    private DocumentService documentService;

    private Borrower borrower;
    private LoanApplication loanApplication;
    private Document document;
    private DocumentUploadEvent documentUploadEvent;

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
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.now())
                .build();

        documentUploadEvent = DocumentUploadEvent.newBuilder()
                .setDocumentId(1L)
                .setBorrowerId(1L)
                .setLoanApplicationId(1L)
                .setDocumentType("INCOME_STATEMENT")
                .setFileName("income.pdf")
                .setFilePath("/documents/income.pdf")
                .setFileSize(1024L)
                .setContentType("application/pdf")
                .setEventTimestamp(LocalDateTime.now().toString())
                .build();
    }

    @Test
    void processDocumentUploadEvent_NewDocument_Success() {
        // Given
        when(documentRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        // When
        documentService.processDocumentUploadEvent(documentUploadEvent);

        // Then
        verify(documentRepository).existsById(1L);
        verify(borrowerRepository).findById(1L);
        verify(loanApplicationRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void processDocumentUploadEvent_ExistingDocument_Skip() {
        // Given
        when(documentRepository.existsById(1L)).thenReturn(true);

        // When
        documentService.processDocumentUploadEvent(documentUploadEvent);

        // Then
        verify(documentRepository).existsById(1L);
        verify(borrowerRepository, never()).findById(anyLong());
        verify(documentRepository, never()).save(any());
    }

    @Test
    void processDocumentUploadEvent_BorrowerNotFound_ThrowsException() {
        // Given
        when(documentRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.processDocumentUploadEvent(documentUploadEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Borrower not found with ID: 1");
    }

    @Test
    void processDocumentUploadEvent_LoanApplicationNotFound_ThrowsException() {
        // Given
        when(documentRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.processDocumentUploadEvent(documentUploadEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Loan application not found with ID: 1");
    }

    @Test
    void processDocumentUploadEvent_NoLoanApplication_Success() {
        // Given
        DocumentUploadEvent eventWithoutLoan = documentUploadEvent.toBuilder()
                .setLoanApplicationId(0L)
                .build();
        
        when(documentRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        // When
        documentService.processDocumentUploadEvent(eventWithoutLoan);

        // Then
        verify(documentRepository).save(any(Document.class));
        verify(loanApplicationRepository, never()).findById(anyLong());
    }

    @Test
    void updateDocumentStatus_ValidDocument_Success() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("officer123")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        when(kafkaEventProducerService.generateEventId()).thenReturn("event-123");

        // When
        documentService.updateDocumentStatus(1L, request);

        // Then
        verify(documentRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
        verify(kafkaEventProducerService).publishDocumentStatusUpdateEvent(any(DocumentStatusUpdateEvent.class));
        
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
        assertThat(document.getStatusUpdatedBy()).isEqualTo("officer123");
        assertThat(document.getStatusUpdatedAt()).isNotNull();
    }

    @Test
    void updateDocumentStatus_DocumentNotFound_ThrowsException() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("officer123")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.updateDocumentStatus(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Document not found with ID: 1");
    }

    @Test
    void updateDocumentStatus_WithRejectionReason_Success() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.REJECTED)
                .rejectionReason("Poor quality document")
                .updatedBy("officer123")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        when(kafkaEventProducerService.generateEventId()).thenReturn("event-123");

        // When
        documentService.updateDocumentStatus(1L, request);

        // Then
        verify(kafkaEventProducerService).publishDocumentStatusUpdateEvent(any(DocumentStatusUpdateEvent.class));
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
    }

    @Test
    void getAllDocuments_ReturnsListOfResponses() {
        // Given
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findAll()).thenReturn(documents);

        // When
        List<DocumentResponse> responses = documentService.getAllDocuments();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getDocumentId()).isEqualTo(1L);
        assertThat(responses.get(0).getFileName()).isEqualTo("income.pdf");
    }

    @Test
    void getDocumentById_ExistingId_ReturnsResponse() {
        // Given
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        // When
        Optional<DocumentResponse> response = documentService.getDocumentById(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getDocumentId()).isEqualTo(1L);
    }

    @Test
    void getDocumentById_NonExistingId_ReturnsEmpty() {
        // Given
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<DocumentResponse> response = documentService.getDocumentById(999L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void getDocumentsByStatus_ReturnsFilteredList() {
        // Given
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByStatus(DocumentStatus.PENDING)).thenReturn(documents);

        // When
        List<DocumentResponse> responses = documentService.getDocumentsByStatus(DocumentStatus.PENDING);

        // Then
        assertThat(responses).hasSize(1);
        verify(documentRepository).findByStatus(DocumentStatus.PENDING);
    }

    @Test
    void getDocumentsByBorrowerId_ReturnsFilteredList() {
        // Given
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByBorrowerBorrowerId(1L)).thenReturn(documents);

        // When
        List<DocumentResponse> responses = documentService.getDocumentsByBorrowerId(1L);

        // Then
        assertThat(responses).hasSize(1);
        verify(documentRepository).findByBorrowerBorrowerId(1L);
    }

    @Test
    void getDocumentsByLoanApplicationId_ReturnsFilteredList() {
        // Given
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByLoanApplicationApplicationId(1L)).thenReturn(documents);

        // When
        List<DocumentResponse> responses = documentService.getDocumentsByLoanApplicationId(1L);

        // Then
        assertThat(responses).hasSize(1);
        verify(documentRepository).findByLoanApplicationApplicationId(1L);
    }

    @Test
    void existsById_ExistingId_ReturnsTrue() {
        // Given
        when(documentRepository.existsById(1L)).thenReturn(true);

        // When
        boolean exists = documentService.existsById(1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingId_ReturnsFalse() {
        // Given
        when(documentRepository.existsById(999L)).thenReturn(false);

        // When
        boolean exists = documentService.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
}
