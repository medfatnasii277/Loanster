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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private Borrower borrower;
    private LoanApplication loanApplication;
    private LoanApplicationDto loanApplicationDto;

    @BeforeEach
    void setUp() {
        borrower = Borrower.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        loanApplication = LoanApplication.builder()
                .id(1L)
                .borrower(borrower)
                .loanType("Personal")
                .loanAmount(BigDecimal.valueOf(10000))
                .loanTermMonths(36)
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(302.89))
                .totalPayment(BigDecimal.valueOf(10904.04))
                .status(LoanApplicationStatus.PENDING)
                .purpose("Home improvement")
                .applicationNumber("LOAN-123456789-001")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loanApplicationDto = LoanApplicationDto.builder()
                .id(1L)
                .borrowerId(1L)
                .borrowerName("John Doe")
                .loanType("Personal")
                .loanAmount(BigDecimal.valueOf(10000))
                .loanTermMonths(36)
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(302.89))
                .totalPayment(BigDecimal.valueOf(10904.04))
                .status(LoanApplicationStatus.PENDING)
                .purpose("Home improvement")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void applyForLoan_ShouldReturnLoanApplicationDto_WhenValidRequest() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(loanApplicationMapper.toEntity(loanApplicationDto)).thenReturn(loanApplication);
        when(loanCalculatorService.calculateMonthlyPayment(any(BigDecimal.class), any(BigDecimal.class), anyInt()))
                .thenReturn(BigDecimal.valueOf(302.89));
        when(loanCalculatorService.calculateTotalPayment(any(BigDecimal.class), anyInt()))
                .thenReturn(BigDecimal.valueOf(10904.04));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);
        when(loanApplicationMapper.toDto(loanApplication)).thenReturn(loanApplicationDto);
        when(eventMapper.toLoanApplicationEvent(loanApplication)).thenReturn(null);

        // Act
        LoanApplicationDto result = loanApplicationService.applyForLoan(1L, loanApplicationDto);

        // Assert
        assertNotNull(result);
        assertEquals(loanApplicationDto.getId(), result.getId());
        assertEquals(loanApplicationDto.getBorrowerId(), result.getBorrowerId());
        assertEquals(loanApplicationDto.getLoanAmount(), result.getLoanAmount());

        verify(borrowerRepository).findById(1L);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
        verify(kafkaEventProducerService).publishLoanApplicationEvent(any());
    }

    @Test
    void applyForLoan_ShouldThrowEntityNotFoundException_WhenBorrowerNotFound() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> loanApplicationService.applyForLoan(1L, loanApplicationDto));

        verify(borrowerRepository).findById(1L);
        verifyNoInteractions(loanApplicationRepository);
        verifyNoInteractions(kafkaEventProducerService);
    }

    @Test
    void getLoanApplication_ShouldReturnLoanApplicationDto_WhenExists() {
        // Arrange
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(loanApplicationMapper.toDto(loanApplication)).thenReturn(loanApplicationDto);

        // Act
        LoanApplicationDto result = loanApplicationService.getLoanApplication(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(loanApplicationDto.getId(), result.getId());
        assertEquals(loanApplicationDto.getBorrowerId(), result.getBorrowerId());

        verify(loanApplicationRepository).findById(1L);
        verify(loanApplicationMapper).toDto(loanApplication);
    }

    @Test
    void getLoanApplication_ShouldThrowEntityNotFoundException_WhenNotFound() {
        // Arrange
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> loanApplicationService.getLoanApplication(1L, 1L));

        verify(loanApplicationRepository).findById(1L);
        verifyNoInteractions(loanApplicationMapper);
    }

    @Test
    void getLoanApplication_ShouldThrowEntityNotFoundException_WhenBorrowerMismatch() {
        // Arrange
        Borrower anotherBorrower = Borrower.builder().id(2L).build();
        LoanApplication loanAppWithDifferentBorrower = LoanApplication.builder()
                .id(1L)
                .borrower(anotherBorrower)
                .build();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanAppWithDifferentBorrower));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> loanApplicationService.getLoanApplication(1L, 1L));

        assertTrue(exception.getMessage().contains("Loan application does not belong to this borrower"));
        verify(loanApplicationRepository).findById(1L);
        verifyNoInteractions(loanApplicationMapper);
    }

    @Test
    void getLoanApplicationsForBorrower_ShouldReturnList() {
        // Arrange
        List<LoanApplication> loanApplications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findByBorrowerId(1L)).thenReturn(loanApplications);
        when(loanApplicationMapper.toDto(loanApplication)).thenReturn(loanApplicationDto);

        // Act
        List<LoanApplicationDto> result = loanApplicationService.getLoanApplicationsForBorrower(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(loanApplicationDto.getId(), result.get(0).getId());

        verify(loanApplicationRepository).findByBorrowerId(1L);
        verify(loanApplicationMapper).toDto(loanApplication);
    }

    @Test
    void getLoanApplicationsByStatus_ShouldReturnFilteredList() {
        // Arrange
        List<LoanApplication> pendingApplications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findByBorrowerIdAndStatus(1L, LoanApplicationStatus.PENDING))
                .thenReturn(pendingApplications);
        when(loanApplicationMapper.toDto(loanApplication)).thenReturn(loanApplicationDto);

        // Act
        List<LoanApplicationDto> result = loanApplicationService.getLoanApplicationsByStatus(1L, LoanApplicationStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LoanApplicationStatus.PENDING, result.get(0).getStatus());

        verify(loanApplicationRepository).findByBorrowerIdAndStatus(1L, LoanApplicationStatus.PENDING);
        verify(loanApplicationMapper).toDto(loanApplication);
    }
}
