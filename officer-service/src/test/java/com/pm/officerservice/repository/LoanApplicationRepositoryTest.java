package com.pm.officerservice.repository;

import com.pm.officerservice.model.Borrower;
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

        borrower = entityManager.persistAndFlush(borrower);

        loanApplication1 = LoanApplication.builder()
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

        loanApplication2 = LoanApplication.builder()
                .applicationId(2L)
                .borrower(borrower)
                .loanAmount(BigDecimal.valueOf(50000))
                .loanTermMonths(24)
                .loanPurpose("Car Purchase")
                .interestRate(BigDecimal.valueOf(4.5))
                .monthlyPayment(BigDecimal.valueOf(2000))
                .status("APPROVED")
                .appliedAtSource(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(loanApplication1);
        entityManager.persistAndFlush(loanApplication2);
    }

    @Test
    void findByBorrowerBorrowerId_ReturnsMatchingApplications() {
        // When
        List<LoanApplication> applications = loanApplicationRepository.findByBorrowerBorrowerId(1L);

        // Then
        assertThat(applications).hasSize(2);
        assertThat(applications).extracting(LoanApplication::getApplicationId).containsExactly(1L, 2L);
    }

    @Test
    void findByBorrowerBorrowerId_NonExistingBorrower_ReturnsEmpty() {
        // When
        List<LoanApplication> applications = loanApplicationRepository.findByBorrowerBorrowerId(999L);

        // Then
        assertThat(applications).isEmpty();
    }

    @Test
    void findByStatus_ReturnsMatchingApplications() {
        // When
        List<LoanApplication> pendingApplications = loanApplicationRepository.findByStatus("PENDING");
        List<LoanApplication> approvedApplications = loanApplicationRepository.findByStatus("APPROVED");

        // Then
        assertThat(pendingApplications).hasSize(1);
        assertThat(pendingApplications.get(0).getApplicationId()).isEqualTo(1L);
        
        assertThat(approvedApplications).hasSize(1);
        assertThat(approvedApplications.get(0).getApplicationId()).isEqualTo(2L);
    }

    @Test
    void findByStatus_NonExistingStatus_ReturnsEmpty() {
        // When
        List<LoanApplication> applications = loanApplicationRepository.findByStatus("REJECTED");

        // Then
        assertThat(applications).isEmpty();
    }

    @Test
    void save_CreatesNewApplication() {
        // Given
        LoanApplication newApplication = LoanApplication.builder()
                .applicationId(3L)
                .borrower(borrower)
                .loanAmount(BigDecimal.valueOf(75000))
                .loanTermMonths(30)
                .loanPurpose("Business Loan")
                .interestRate(BigDecimal.valueOf(6.0))
                .monthlyPayment(BigDecimal.valueOf(2500))
                .status("UNDER_REVIEW")
                .appliedAtSource(LocalDateTime.now())
                .build();

        // When
        LoanApplication saved = loanApplicationRepository.save(newApplication);

        // Then
        assertThat(saved.getApplicationId()).isEqualTo(3L);
        assertThat(saved.getLoanPurpose()).isEqualTo("Business Loan");
        assertThat(saved.getStatus()).isEqualTo("UNDER_REVIEW");
    }

    @Test
    void findById_ExistingId_ReturnsApplication() {
        // When
        var foundApplication = loanApplicationRepository.findById(1L);

        // Then
        assertThat(foundApplication).isPresent();
        assertThat(foundApplication.get().getApplicationId()).isEqualTo(1L);
        assertThat(foundApplication.get().getLoanPurpose()).isEqualTo("Home Purchase");
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // When
        var foundApplication = loanApplicationRepository.findById(999L);

        // Then
        assertThat(foundApplication).isEmpty();
    }

    @Test
    void existsById_ExistingId_ReturnsTrue() {
        // When
        boolean exists = loanApplicationRepository.existsById(1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingId_ReturnsFalse() {
        // When
        boolean exists = loanApplicationRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
}
