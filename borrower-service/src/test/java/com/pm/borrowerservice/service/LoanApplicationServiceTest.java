package com.pm.borrowerservice.service;

import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.mapper.LoanApplicationMapper;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.repository.LoanApplicationRepository;
import com.pm.borrowerservice.util.EventMapper;
import jakarta.persistence.EntityNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private LoanApplicationMapper loanApplicationMapper;

    @Mock
    private LoanCalculatorService loanCalculatorService;

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private Borrower testBorrower;
    private LoanApplication testLoanApplication;
    private LoanApplicationDto testLoanApplicationDto;

    @BeforeEach
    void setUp() {
        testBorrower = Borrower.builder()
                .id(1L)
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .annualIncome(75000.0)
                .build();

        testLoanApplication = LoanApplication.builder()
                .id(1L)
                .borrower(testBorrower)
                .loanType("PERSONAL")
                .loanAmount(new BigDecimal("10000.00"))
                .loanTermMonths(24)
                .interestRate(new BigDecimal("5.5"))
                .monthlyPayment(new BigDecimal("450.00"))
                .totalPayment(new BigDecimal("10800.00"))
                .status(LoanApplicationStatus.PENDING)
                .applicationNumber("APP-001")
                .purpose("Home improvement")
                .notes("First time borrower")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testLoanApplicationDto = LoanApplicationDto.builder()
                .id(1L)
                .borrowerId(1L)
                .loanType("PERSONAL")
                .loanAmount(new BigDecimal("10000.00"))
                .loanTermMonths(24)
                .interestRate(new BigDecimal("5.5"))
                .monthlyPayment(new BigDecimal("450.00"))
                .totalPayment(new BigDecimal("10800.00"))
                .status(LoanApplicationStatus.PENDING)
                .applicationNumber("APP-001")
                .purpose("Home improvement")
                .notes("First time borrower")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void applyForLoan_ShouldReturnLoanApplicationDto_WhenValidRequest() {
        // Given
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));
        when(loanApplicationMapper.toEntity(testLoanApplicationDto)).thenReturn(testLoanApplication);
        when(loanCalculatorService.calculateMonthlyPayment(
                any(BigDecimal.class), any(BigDecimal.class), anyInt()))
                .thenReturn(new BigDecimal("450.00"));
        when(loanCalculatorService.calculateTotalPayment(
                any(BigDecimal.class), anyInt()))
                .thenReturn(new BigDecimal("10800.00"));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(testLoanApplication);
        when(loanApplicationMapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDto);
        when(eventMapper.toLoanApplicationEvent(any())).thenReturn(any());

        // When
        LoanApplicationDto result = loanApplicationService.applyForLoan(1L, testLoanApplicationDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLoanType()).isEqualTo("PERSONAL");
        assertThat(result.getLoanAmount()).isEqualTo(new BigDecimal("10000.00"));
        assertThat(result.getStatus()).isEqualTo(LoanApplicationStatus.PENDING);

        verify(borrowerRepository).findById(1L);
        verify(loanCalculatorService).calculateMonthlyPayment(
                any(BigDecimal.class), any(BigDecimal.class), anyInt());
        verify(loanCalculatorService).calculateTotalPayment(
                any(BigDecimal.class), anyInt());
        verify(loanApplicationRepository).save(any(LoanApplication.class));
        verify(kafkaEventProducerService).publishLoanApplicationEvent(any());
    }

    @Test
    void applyForLoan_ShouldThrowEntityNotFoundException_WhenBorrowerNotExists() {
        // Given
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loanApplicationService.applyForLoan(1L, testLoanApplicationDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Borrower not found");

        verify(borrowerRepository).findById(1L);
        verify(loanApplicationRepository, never()).save(any(LoanApplication.class));
        verify(kafkaEventProducerService, never()).publishLoanApplicationEvent(any());
    }

    @Test
    void getLoanApplication_ShouldReturnLoanApplicationDto_WhenLoanApplicationExists() {
        // Given
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(testLoanApplication));
        when(loanApplicationMapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDto);

        // When
        LoanApplicationDto result = loanApplicationService.getLoanApplication(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoanType()).isEqualTo("PERSONAL");

        verify(loanApplicationRepository).findById(1L);
        verify(loanApplicationMapper).toDto(testLoanApplication);
    }

    @Test
    void getLoanApplication_ShouldThrowEntityNotFoundException_WhenLoanApplicationNotExists() {
        // Given
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loanApplicationService.getLoanApplication(1L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan application not found");

        verify(loanApplicationRepository).findById(1L);
        verify(loanApplicationMapper, never()).toDto(any(LoanApplication.class));
    }

    @Test
    void getLoanApplicationsForBorrower_ShouldReturnListOfLoanApplicationDto() {
        // Given
        List<LoanApplication> loanApplications = Arrays.asList(testLoanApplication);
        when(loanApplicationRepository.findByBorrowerId(1L)).thenReturn(loanApplications);
        when(loanApplicationMapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDto);

        // When
        List<LoanApplicationDto> result = loanApplicationService.getLoanApplicationsForBorrower(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBorrowerId()).isEqualTo(1L);
        assertThat(result.get(0).getLoanType()).isEqualTo("PERSONAL");

        verify(loanApplicationRepository).findByBorrowerId(1L);
        verify(loanApplicationMapper).toDto(testLoanApplication);
    }

    @Test
    void getLoanApplicationsByStatus_ShouldReturnListOfLoanApplicationDto() {
        // Given
        List<LoanApplication> loanApplications = Arrays.asList(testLoanApplication);
        when(loanApplicationRepository.findByBorrowerIdAndStatus(1L, LoanApplicationStatus.PENDING))
                .thenReturn(loanApplications);
        when(loanApplicationMapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDto);

        // When
        List<LoanApplicationDto> result = loanApplicationService.getLoanApplicationsByStatus(1L, LoanApplicationStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(LoanApplicationStatus.PENDING);

        verify(loanApplicationRepository).findByBorrowerIdAndStatus(1L, LoanApplicationStatus.PENDING);
        verify(loanApplicationMapper).toDto(testLoanApplication);
    }
}