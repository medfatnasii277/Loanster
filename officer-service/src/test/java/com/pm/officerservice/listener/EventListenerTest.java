package com.pm.officerservice.listener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.borrowerservice.events.DocumentUploadEvent;
import com.pm.borrowerservice.events.LoanApplicationEvent;
import com.pm.officerservice.service.BorrowerService;
import com.pm.officerservice.service.DocumentService;
import com.pm.officerservice.service.LoanApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock
    private BorrowerService borrowerService;

    @Mock
    private LoanApplicationService loanApplicationService;

    @Mock
    private DocumentService documentService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private EventListener eventListener;

    private BorrowerCreatedEvent borrowerCreatedEvent;
    private LoanApplicationEvent loanApplicationEvent;
    private DocumentUploadEvent documentUploadEvent;

    @BeforeEach
    void setUp() {
        borrowerCreatedEvent = BorrowerCreatedEvent.newBuilder()
                .setBorrowerId(1L)
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com")
                .setPhoneNumber("1234567890")
                .setDateOfBirth("1990-01-01")
                .setSsn("123-45-6789")
                .setAddress("123 Main St")
                .setCity("New York")
                .setState("NY")
                .setZipCode("10001")
                .setAnnualIncome(50000.0)
                .setEmploymentStatus("EMPLOYED")
                .setEmployerName("ABC Corp")
                .setEmploymentYears(5)
                .setCreatedAt(LocalDateTime.now().toString())
                .build();

        loanApplicationEvent = LoanApplicationEvent.newBuilder()
                .setApplicationId(1L)
                .setBorrowerId(1L)
                .setLoanAmount(100000.0)
                .setLoanTermMonths(36)
                .setLoanPurpose("Home Purchase")
                .setInterestRate(5.5)
                .setMonthlyPayment(3000.0)
                .setStatus("PENDING")
                .setAppliedAt(LocalDateTime.now().toString())
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
    void handleBorrowerCreatedEvent_ValidEvent_Success() throws Exception {
        // Given
        byte[] eventData = borrowerCreatedEvent.toByteArray();
        String key = "borrower-1";
        String topic = "borrower-created";

        doNothing().when(borrowerService).processBorrowerCreatedEvent(any(BorrowerCreatedEvent.class));

        // When
        eventListener.handleBorrowerCreatedEvent(eventData, key, topic, acknowledgment);

        // Then
        verify(borrowerService).processBorrowerCreatedEvent(any(BorrowerCreatedEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleBorrowerCreatedEvent_InvalidProtobuf_AcknowledgesWithoutProcessing() {
        // Given
        byte[] invalidEventData = "invalid protobuf data".getBytes();
        String key = "borrower-1";
        String topic = "borrower-created";

        // When
        eventListener.handleBorrowerCreatedEvent(invalidEventData, key, topic, acknowledgment);

        // Then
        verify(borrowerService, never()).processBorrowerCreatedEvent(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleBorrowerCreatedEvent_ServiceException_AcknowledgesWithoutRetry() throws Exception {
        // Given
        byte[] eventData = borrowerCreatedEvent.toByteArray();
        String key = "borrower-1";
        String topic = "borrower-created";

        doThrow(new RuntimeException("Service error")).when(borrowerService).processBorrowerCreatedEvent(any());

        // When
        eventListener.handleBorrowerCreatedEvent(eventData, key, topic, acknowledgment);

        // Then
        verify(borrowerService).processBorrowerCreatedEvent(any(BorrowerCreatedEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleLoanApplicationEvent_ValidEvent_Success() throws Exception {
        // Given
        byte[] eventData = loanApplicationEvent.toByteArray();
        String key = "loan-1";
        String topic = "loan-application";

        doNothing().when(loanApplicationService).processLoanApplicationEvent(any(LoanApplicationEvent.class));

        // When
        eventListener.handleLoanApplicationEvent(eventData, key, topic, acknowledgment);

        // Then
        verify(loanApplicationService).processLoanApplicationEvent(any(LoanApplicationEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleLoanApplicationEvent_InvalidProtobuf_AcknowledgesWithoutProcessing() {
        // Given
        byte[] invalidEventData = "invalid protobuf data".getBytes();
        String key = "loan-1";
        String topic = "loan-application";

        // When
        eventListener.handleLoanApplicationEvent(invalidEventData, key, topic, acknowledgment);

        // Then
        verify(loanApplicationService, never()).processLoanApplicationEvent(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleLoanApplicationEvent_ServiceException_AcknowledgesWithoutRetry() throws Exception {
        // Given
        byte[] eventData = loanApplicationEvent.toByteArray();
        String key = "loan-1";
        String topic = "loan-application";

        doThrow(new RuntimeException("Service error")).when(loanApplicationService).processLoanApplicationEvent(any());

        // When
        eventListener.handleLoanApplicationEvent(eventData, key, topic, acknowledgment);

        // Then
        verify(loanApplicationService).processLoanApplicationEvent(any(LoanApplicationEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleDocumentUploadEvent_ValidEvent_Success() throws Exception {
        // Given
        byte[] eventData = documentUploadEvent.toByteArray();
        String key = "document-1";
        String topic = "documents-upload";

        doNothing().when(documentService).processDocumentUploadEvent(any(DocumentUploadEvent.class));

        // When
        eventListener.handleDocumentUploadEvent(eventData, key, topic, acknowledgment);

        // Then
        verify(documentService).processDocumentUploadEvent(any(DocumentUploadEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleDocumentUploadEvent_InvalidProtobuf_AcknowledgesWithoutProcessing() {
        // Given
        byte[] invalidEventData = "invalid protobuf data".getBytes();
        String key = "document-1";
        String topic = "documents-upload";

        // When
        eventListener.handleDocumentUploadEvent(invalidEventData, key, topic, acknowledgment);

        // Then
        verify(documentService, never()).processDocumentUploadEvent(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleDocumentUploadEvent_ServiceException_AcknowledgesWithoutRetry() throws Exception {
        // Given
        byte[] eventData = documentUploadEvent.toByteArray();
        String key = "document-1";
        String topic = "documents-upload";

        doThrow(new RuntimeException("Service error")).when(documentService).processDocumentUploadEvent(any());

        // When
        eventListener.handleDocumentUploadEvent(eventData, key, topic, acknowledgment);

        // Then
        verify(documentService).processDocumentUploadEvent(any(DocumentUploadEvent.class));
        verify(acknowledgment).acknowledge();
    }
}
