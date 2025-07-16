package com.pm.borrowerservice.repository;

import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LoanApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    private Borrower borrower;
    private LoanApplication loanApplication1;
    private LoanApplication loanApplication2;

    @BeforeEach
    void setUp() {
        borrower = Borrower.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(75000.0)
                .employmentStatus("Employed")
                .employerName("Tech Corp")
                .employmentYears(5)
                .build();

        entityManager.persistAndFlush(borrower);

        loanApplication1 = LoanApplication.builder()
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
                .build();

        loanApplication2 = LoanApplication.builder()
                .borrower(borrower)
                .loanType("Auto")
                .loanAmount(BigDecimal.valueOf(25000))
                .loanTermMonths(60)
                .interestRate(BigDecimal.valueOf(4.5))
                .monthlyPayment(BigDecimal.valueOf(465.54))
                .totalPayment(BigDecimal.valueOf(27932.40))
                .status(LoanApplicationStatus.APPROVED)
                .purpose("Car purchase")
                .applicationNumber("LOAN-123456789-002")
                .build();

        entityManager.persistAndFlush(loanApplication1);
        entityManager.persistAndFlush(loanApplication2);
    }

    @Test
    void findByBorrowerId_ShouldReturnLoanApplications() {
        // Act
        List<LoanApplication> result = loanApplicationRepository.findByBorrowerId(borrower.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(app -> app.getBorrower().getId().equals(borrower.getId())));
    }

    @Test
    void findByBorrowerId_ShouldReturnEmptyList_WhenNoBorrower() {
        // Act
        List<LoanApplication> result = loanApplicationRepository.findByBorrowerId(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByApplicationNumber_ShouldReturnLoanApplication() {
        // Act
        Optional<LoanApplication> result = loanApplicationRepository.findByApplicationNumber("LOAN-123456789-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(loanApplication1.getId(), result.get().getId());
        assertEquals("LOAN-123456789-001", result.get().getApplicationNumber());
    }

    @Test
    void findByApplicationNumber_ShouldReturnEmpty_WhenNotFound() {
        // Act
        Optional<LoanApplication> result = loanApplicationRepository.findByApplicationNumber("NON-EXISTENT");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByBorrowerIdAndStatus_ShouldReturnFilteredApplications() {
        // Act
        List<LoanApplication> pendingResults = loanApplicationRepository.findByBorrowerIdAndStatus(
                borrower.getId(), LoanApplicationStatus.PENDING);
        
        List<LoanApplication> approvedResults = loanApplicationRepository.findByBorrowerIdAndStatus(
                borrower.getId(), LoanApplicationStatus.APPROVED);

        // Assert
        assertEquals(1, pendingResults.size());
        assertEquals(LoanApplicationStatus.PENDING, pendingResults.get(0).getStatus());
        assertEquals("Personal", pendingResults.get(0).getLoanType());

        assertEquals(1, approvedResults.size());
        assertEquals(LoanApplicationStatus.APPROVED, approvedResults.get(0).getStatus());
        assertEquals("Auto", approvedResults.get(0).getLoanType());
    }

    @Test
    void findByBorrowerIdAndStatus_ShouldReturnEmpty_WhenNoMatchingStatus() {
        // Act
        List<LoanApplication> result = loanApplicationRepository.findByBorrowerIdAndStatus(
                borrower.getId(), LoanApplicationStatus.REJECTED);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldPersistLoanApplication() {
        // Arrange
        LoanApplication newApplication = LoanApplication.builder()
                .borrower(borrower)
                .loanType("Mortgage")
                .loanAmount(BigDecimal.valueOf(300000))
                .loanTermMonths(360)
                .interestRate(BigDecimal.valueOf(3.5))
                .monthlyPayment(BigDecimal.valueOf(1347.13))
                .totalPayment(BigDecimal.valueOf(484966.80))
                .status(LoanApplicationStatus.PENDING)
                .purpose("Home purchase")
                .applicationNumber("LOAN-123456789-003")
                .build();

        // Act
        LoanApplication saved = loanApplicationRepository.save(newApplication);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Mortgage", saved.getLoanType());
        assertEquals(BigDecimal.valueOf(300000), saved.getLoanAmount());
        assertEquals(LoanApplicationStatus.PENDING, saved.getStatus());

        // Verify it can be found
        Optional<LoanApplication> found = loanApplicationRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void delete_ShouldRemoveLoanApplication() {
        // Arrange
        Long applicationId = loanApplication1.getId();
        assertTrue(loanApplicationRepository.findById(applicationId).isPresent());

        // Act
        loanApplicationRepository.delete(loanApplication1);

        // Assert
        assertFalse(loanApplicationRepository.findById(applicationId).isPresent());
    }

    @Test
    void findAll_ShouldReturnAllApplications() {
        // Act
        List<LoanApplication> result = loanApplicationRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void loanApplication_ShouldHaveCorrectRelationshipWithBorrower() {
        // Act
        Optional<LoanApplication> result = loanApplicationRepository.findById(loanApplication1.getId());

        // Assert
        assertTrue(result.isPresent());
        LoanApplication app = result.get();
        assertNotNull(app.getBorrower());
        assertEquals(borrower.getId(), app.getBorrower().getId());
        assertEquals("John", app.getBorrower().getFirstName());
        assertEquals("Doe", app.getBorrower().getLastName());
    }

    @Test
    void applicationNumber_ShouldBeUnique() {
        // Arrange
        LoanApplication duplicateApplication = LoanApplication.builder()
                .borrower(borrower)
                .loanType("Personal")
                .loanAmount(BigDecimal.valueOf(5000))
                .loanTermMonths(24)
                .interestRate(BigDecimal.valueOf(6.0))
                .status(LoanApplicationStatus.PENDING)
                .purpose("Test")
                .applicationNumber("LOAN-123456789-001") // Same as existing
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            loanApplicationRepository.saveAndFlush(duplicateApplication);
        });
    }
}
