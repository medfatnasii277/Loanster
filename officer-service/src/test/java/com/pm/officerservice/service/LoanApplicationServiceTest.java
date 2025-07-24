package com.pm.officerservice.service;

import com.pm.borrowerservice.events.LoanApplicationEvent;
import com.pm.officerservice.dto.LoanApplicationResponse;
import com.pm.officerservice.dto.LoanStatusUpdateRequest;
import com.pm.officerservice.events.LoanStatusUpdateEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.model.LoanApplicationStatus;
import com.pm.officerservice.repository.BorrowerRepository;
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
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private Borrower borrower;
    private LoanApplication loanApplication;
    private LoanApplicationEvent loanApplicationEvent;

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
    }

    @Test
    void processLoanApplicationEvent_NewApplication_Success() {
        // Given
        when(loanApplicationRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // When
        loanApplicationService.processLoanApplicationEvent(loanApplicationEvent);

        // Then
        verify(loanApplicationRepository).existsById(1L);
        verify(borrowerRepository).findById(1L);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    void processLoanApplicationEvent_ExistingApplication_Skip() {
        // Given
        when(loanApplicationRepository.existsById(1L)).thenReturn(true);

        // When
        loanApplicationService.processLoanApplicationEvent(loanApplicationEvent);

        // Then
        verify(loanApplicationRepository).existsById(1L);
        verify(borrowerRepository, never()).findById(anyLong());
        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    void processLoanApplicationEvent_BorrowerNotFound_ThrowsException() {
        // Given
        when(loanApplicationRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loanApplicationService.processLoanApplicationEvent(loanApplicationEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Borrower not found with ID: 1");
    }

    @Test
    void updateLoanStatus_ValidApplication_Success() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("officer123")
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);
        when(kafkaEventProducerService.generateEventId()).thenReturn("event-123");

        // When
        loanApplicationService.updateLoanStatus(1L, request);

        // Then
        verify(loanApplicationRepository).findById(1L);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
        verify(kafkaEventProducerService).publishLoanStatusUpdateEvent(any(LoanStatusUpdateEvent.class));
        
        assertThat(loanApplication.getStatus()).isEqualTo("APPROVED");
        assertThat(loanApplication.getStatusUpdatedBy()).isEqualTo("officer123");
        assertThat(loanApplication.getStatusUpdatedAt()).isNotNull();
    }

    @Test
    void updateLoanStatus_ApplicationNotFound_ThrowsException() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("officer123")
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loanApplicationService.updateLoanStatus(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Loan application not found with ID: 1");
    }

    @Test
    void updateLoanStatus_WithRejectionReason_Success() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.REJECTED)
                .rejectionReason("Insufficient income")
                .updatedBy("officer123")
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);
        when(kafkaEventProducerService.generateEventId()).thenReturn("event-123");

        // When
        loanApplicationService.updateLoanStatus(1L, request);

        // Then
        verify(kafkaEventProducerService).publishLoanStatusUpdateEvent(any(LoanStatusUpdateEvent.class));
        assertThat(loanApplication.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void getAllLoanApplications_ReturnsListOfResponses() {
        // Given
        List<LoanApplication> applications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findAll()).thenReturn(applications);

        // When
        List<LoanApplicationResponse> responses = loanApplicationService.getAllLoanApplications();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getApplicationId()).isEqualTo(1L);
        assertThat(responses.get(0).getBorrowerName()).isEqualTo("John Doe");
    }

    @Test
    void getLoanApplicationById_ExistingId_ReturnsResponse() {
        // Given
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        // When
        Optional<LoanApplicationResponse> response = loanApplicationService.getLoanApplicationById(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getApplicationId()).isEqualTo(1L);
    }

    @Test
    void getLoanApplicationById_NonExistingId_ReturnsEmpty() {
        // Given
        when(loanApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<LoanApplicationResponse> response = loanApplicationService.getLoanApplicationById(999L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void getLoanApplicationsByStatus_ReturnsFilteredList() {
        // Given
        List<LoanApplication> applications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findByStatus("PENDING")).thenReturn(applications);

        // When
        List<LoanApplicationResponse> responses = loanApplicationService.getLoanApplicationsByStatus(LoanApplicationStatus.PENDING);

        // Then
        assertThat(responses).hasSize(1);
        verify(loanApplicationRepository).findByStatus("PENDING");
    }

    @Test
    void getLoanApplicationsByBorrowerId_ReturnsFilteredList() {
        // Given
        List<LoanApplication> applications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findByBorrowerBorrowerId(1L)).thenReturn(applications);

        // When
        List<LoanApplicationResponse> responses = loanApplicationService.getLoanApplicationsByBorrowerId(1L);

        // Then
        assertThat(responses).hasSize(1);
        verify(loanApplicationRepository).findByBorrowerBorrowerId(1L);
    }

    @Test
    void existsById_ExistingId_ReturnsTrue() {
        // Given
        when(loanApplicationRepository.existsById(1L)).thenReturn(true);

        // When
        boolean exists = loanApplicationService.existsById(1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingId_ReturnsFalse() {
        // Given
        when(loanApplicationRepository.existsById(999L)).thenReturn(false);

        // When
        boolean exists = loanApplicationService.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
}
